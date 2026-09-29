package library.service;

import library.model.Book;
import library.model.Member;
import library.model.Transaction;
import library.storage.DataStore;
import library.storage.DataStore.LibraryData;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LibraryService {
    public static final int LOAN_DAYS = 14;
    public static final double FINE_PER_DAY = 5.0;

    private final DataStore dataStore;
    private final LibraryData data;

    public LibraryService() {
        this.dataStore = new DataStore();
        this.data = dataStore.load();
    }

    private void persist() {
        dataStore.save(data);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(data.books);
    }

    public Book getBookById(int id) {
        return data.books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return data.books.stream()
                .filter(b -> contains(b.getTitle(), q)
                        || contains(b.getAuthor(), q)
                        || contains(b.getIsbn(), q)
                        || String.valueOf(b.getId()).equals(q))
                .collect(Collectors.toList());
    }

    public Book addBook(String isbn, String title, String author, int totalCopies) {
        validateBookInput(isbn, title, author, totalCopies);
        Book book = new Book(data.nextBookId++, isbn.trim(), title.trim(), author.trim(),
                totalCopies, totalCopies);
        data.books.add(book);
        persist();
        return book;
    }

    public void updateBook(int id, String isbn, String title, String author, int totalCopies) {
        Book book = requireBook(id);
        validateBookInput(isbn, title, author, totalCopies);
        int issued = book.getTotalCopies() - book.getAvailableCopies();
        if (totalCopies < issued) {
            throw new IllegalArgumentException(
                    "Total copies cannot be less than currently issued copies (" + issued + ").");
        }
        book.setIsbn(isbn.trim());
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies - issued);
        persist();
    }

    public void deleteBook(int id) {
        Book book = requireBook(id);
        boolean hasActive = data.transactions.stream()
                .anyMatch(t -> t.getBookId() == id && t.isActive());
        if (hasActive) {
            throw new IllegalStateException("Cannot delete a book that still has active issues.");
        }
        data.books.remove(book);
        persist();
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(data.members);
    }

    public Member getMemberById(int id) {
        return data.members.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    public List<Member> searchMembers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMembers();
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return data.members.stream()
                .filter(m -> contains(m.getName(), q)
                        || contains(m.getEmail(), q)
                        || contains(m.getPhone(), q)
                        || String.valueOf(m.getId()).equals(q))
                .collect(Collectors.toList());
    }

    public Member addMember(String name, String email, String phone) {
        validateMemberInput(name, email, phone);
        Member member = new Member(data.nextMemberId++, name.trim(), email.trim(), phone.trim());
        data.members.add(member);
        persist();
        return member;
    }

    public void updateMember(int id, String name, String email, String phone) {
        Member member = requireMember(id);
        validateMemberInput(name, email, phone);
        member.setName(name.trim());
        member.setEmail(email.trim());
        member.setPhone(phone.trim());
        persist();
    }

    public void deleteMember(int id) {
        Member member = requireMember(id);
        boolean hasActive = data.transactions.stream()
                .anyMatch(t -> t.getMemberId() == id && t.isActive());
        if (hasActive) {
            throw new IllegalStateException("Cannot delete a member who still has active borrowed books.");
        }
        data.members.remove(member);
        persist();
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(data.transactions);
    }

    public List<Transaction> getActiveTransactions() {
        return data.transactions.stream().filter(Transaction::isActive).collect(Collectors.toList());
    }

    public List<Transaction> getOverdueTransactions() {
        LocalDate today = LocalDate.now();
        return data.transactions.stream()
                .filter(Transaction::isActive)
                .filter(t -> t.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    public Transaction issueBook(int bookId, int memberId) {
        Book book = requireBook(bookId);
        requireMember(memberId);
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies for this book.");
        }
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(LOAN_DAYS);
        Transaction tx = new Transaction(data.nextTransactionId++, bookId, memberId, issueDate, dueDate);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        data.transactions.add(tx);
        persist();
        return tx;
    }

    public Transaction returnBook(int transactionId) {
        Transaction tx = data.transactions.stream()
                .filter(t -> t.getId() == transactionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        if (!tx.isActive()) {
            throw new IllegalStateException("This book has already been returned.");
        }
        Book book = requireBook(tx.getBookId());
        LocalDate returnDate = LocalDate.now();
        tx.setReturnDate(returnDate);
        long daysLate = ChronoUnit.DAYS.between(tx.getDueDate(), returnDate);
        double fine = daysLate > 0 ? daysLate * FINE_PER_DAY : 0.0;
        tx.setFine(fine);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        persist();
        return tx;
    }

    public int getBookCount() {
        return data.books.size();
    }

    public int getMemberCount() {
        return data.members.size();
    }

    public int getIssuedCount() {
        return (int) data.transactions.stream().filter(Transaction::isActive).count();
    }

    public int getOverdueCount() {
        return getOverdueTransactions().size();
    }

    public double calculateCurrentFine(Transaction tx) {
        if (!tx.isActive()) {
            return tx.getFine();
        }
        long daysLate = ChronoUnit.DAYS.between(tx.getDueDate(), LocalDate.now());
        return daysLate > 0 ? daysLate * FINE_PER_DAY : 0.0;
    }

    private Book requireBook(int id) {
        Book book = getBookById(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found: " + id);
        }
        return book;
    }

    private Member requireMember(int id) {
        Member member = getMemberById(id);
        if (member == null) {
            throw new IllegalArgumentException("Member not found: " + id);
        }
        return member;
    }

    private void validateBookInput(String isbn, String title, String author, int totalCopies) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required.");
        }
        if (totalCopies < 1) {
            throw new IllegalArgumentException("Total copies must be at least 1.");
        }
    }

    private void validateMemberInput(String name, String email, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone is required.");
        }
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}