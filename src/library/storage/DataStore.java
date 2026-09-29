package library.storage;

import library.model.Book;
import library.model.Member;
import library.model.Transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + File.separator + "library.dat";

    public static class LibraryData implements Serializable {
        private static final long serialVersionUID = 1L;

        public List<Book> books = new ArrayList<>();
        public List<Member> members = new ArrayList<>();
        public List<Transaction> transactions = new ArrayList<>();
        public int nextBookId = 1;
        public int nextMemberId = 1;
        public int nextTransactionId = 1;
    }

    public LibraryData load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            ensureDataDir();
            return new LibraryData();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof LibraryData) {
                return (LibraryData) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load data, starting fresh: " + e.getMessage());
        }
        return new LibraryData();
    }

    public void save(LibraryData data) {
        ensureDataDir();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save library data: " + e.getMessage(), e);
        }
    }

    private void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Could not create data directory: " + DATA_DIR);
        }
    }
}