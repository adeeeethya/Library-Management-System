package com.example.library;

import com.example.library.dao.BookDao;
import com.example.library.dao.LoanDao;
import com.example.library.dao.MemberDao;
import com.example.library.model.Book;
import com.example.library.model.Member;
import com.example.library.web.WebServer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("web")) {
            int port = resolvePort(args);
            WebServer.start(port);
            System.out.println("Web server running on http://localhost:" + port);
        } else {
            new Main().run();
        }
    }

    private static int resolvePort(String[] args) {
        // Priority: CLI arg > system property server.port > env PORT > default 8080
        if (args.length > 1) {
            try { return Integer.parseInt(args[1]); } catch (NumberFormatException ignored) { }
        }
        String sys = System.getProperty("server.port");
        if (sys != null) {
            try { return Integer.parseInt(sys); } catch (NumberFormatException ignored) { }
        }
        String env = System.getenv("PORT");
        if (env != null) {
            try { return Integer.parseInt(env); } catch (NumberFormatException ignored) { }
        }
        return 8080;
    }

    private final BookDao bookDao = new BookDao();
    private final MemberDao memberDao = new MemberDao();
    private final LoanDao loanDao = new LoanDao();

    private void run() {
        while (true) {
            System.out.println("\nLibrary Management");
            System.out.println("1. List books");
            System.out.println("2. Add book");
            System.out.println("3. List members");
            System.out.println("4. Add member");
            System.out.println("5. Issue loan");
            System.out.println("6. Return loan");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String choice = SCANNER.nextLine().trim();
            try {
                switch (choice) {
                    case "1": listBooks(); break;
                    case "2": addBook(); break;
                    case "3": listMembers(); break;
                    case "4": addMember(); break;
                    case "5": issueLoan(); break;
                    case "6": returnLoan(); break;
                    case "0": System.out.println("Bye!"); return;
                    default: System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void listBooks() throws SQLException {
        List<Book> list = bookDao.listAll();
        if (list.isEmpty()) { System.out.println("No books found."); return; }
        for (Book b : list) {
            System.out.printf("#%d %s - %s (%s) | available %d/%d\n",
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCopiesAvailable(), b.getCopiesTotal());
        }
    }

    private void addBook() throws SQLException {
        System.out.print("ISBN: ");
        String isbn = SCANNER.nextLine().trim();
        System.out.print("Title: ");
        String title = SCANNER.nextLine().trim();
        System.out.print("Author: ");
        String author = SCANNER.nextLine().trim();
        System.out.print("Copies total: ");
        int total = Integer.parseInt(SCANNER.nextLine().trim());
        Book b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setAuthor(author);
        b.setCopiesTotal(total);
        b.setCopiesAvailable(total);
        bookDao.create(b);
        System.out.println("Added book with ID: " + b.getId());
    }

    private void listMembers() throws SQLException {
        List<Member> list = memberDao.listAll();
        if (list.isEmpty()) { System.out.println("No members found."); return; }
        for (Member m : list) {
            System.out.printf("#%d %s | %s | %s\n", m.getId(), m.getName(), m.getEmail(), m.getPhone());
        }
    }

    private void addMember() throws SQLException {
        System.out.print("Name: ");
        String name = SCANNER.nextLine().trim();
        System.out.print("Email: ");
        String email = SCANNER.nextLine().trim();
        System.out.print("Phone: ");
        String phone = SCANNER.nextLine().trim();
        Member m = new Member();
        m.setName(name);
        m.setEmail(email);
        m.setPhone(phone);
        memberDao.create(m);
        System.out.println("Added member with ID: " + m.getId());
    }

    private void issueLoan() throws SQLException {
        System.out.print("Book ID: ");
        long bookId = Long.parseLong(SCANNER.nextLine().trim());
        System.out.print("Member ID: ");
        long memberId = Long.parseLong(SCANNER.nextLine().trim());
        System.out.print("Due in days: ");
        int days = Integer.parseInt(SCANNER.nextLine().trim());
        LocalDate due = LocalDate.now().plusDays(days);
        loanDao.createLoan(bookId, memberId, due);
        System.out.println("Loan created, due " + due);
    }

    private void returnLoan() throws SQLException {
        System.out.print("Loan ID: ");
        long loanId = Long.parseLong(SCANNER.nextLine().trim());
        loanDao.returnLoan(loanId);
        System.out.println("Loan returned.");
    }
}


