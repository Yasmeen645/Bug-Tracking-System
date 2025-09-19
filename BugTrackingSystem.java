// File: BugTrackingSystem.java
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;


/*
  Complete Bug Tracking System (single-file)
  - Main class: BugTrackingSystem (has public static void main)
  - Uses serialization to save/load users & bugs
  - GUI: Login + Admin/Tester/Developer/Project Manager dashboards
  - Improved look & feel (Nimbus), JTable-based dashboards
*/

// ---------------------- Enums ----------------------
enum Role {
    ADMIN, TESTER, DEVELOPER, PROJECT_MANAGER
}

enum BugStatus {
    OPEN, IN_PROGRESS, CLOSED
}

enum BugPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum BugLevel {
    MINOR, MAJOR, BLOCKER
}

// ---------------------- User ----------------------
class User implements Serializable {
    private String username;
    private String password;
    private Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}

// ---------------------- Bug ----------------------
class Bug implements Serializable {
    private int id;
    private String name;
    private String type;
    private BugPriority priority;
    private BugLevel level;
    private String projectName;
    private Date date;
    private BugStatus status;
    private String assignedDeveloper;
    private String screenshotPath;
    private String reportedBy;

    public Bug(int id, String name, String type, BugPriority priority, BugLevel level,
               String projectName, Date date, BugStatus status, String assignedDeveloper,
               String screenshotPath, String reportedBy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.level = level;
        this.projectName = projectName;
        this.date = date;
        this.status = status;
        this.assignedDeveloper = assignedDeveloper;
        this.screenshotPath = screenshotPath;
        this.reportedBy = reportedBy;
    }

    // Getters / setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public BugPriority getPriority() { return priority; }
    public BugLevel getLevel() { return level; }
    public String getProjectName() { return projectName; }
    public Date getDate() { return date; }
    public BugStatus getStatus() { return status; }
    public String getAssignedDeveloper() { return assignedDeveloper; }
    public String getScreenshotPath() { return screenshotPath; }
    public String getReportedBy() { return reportedBy; }

    public void setStatus(BugStatus status) { this.status = status; }
    public void setAssignedDeveloper(String assignedDeveloper) { this.assignedDeveloper = assignedDeveloper; }

    // Convenience for table rows (admin/pm/tester)
    public Object[] toTableRowForAdmin() {
        return new Object[]{id, name, type, priority, status, projectName, assignedDeveloper, reportedBy};
    }

    public Object[] toTableRowForTester() {
        return new Object[]{id, name, type, priority, status, projectName, assignedDeveloper};
    }

    public Object[] toTableRowForDeveloper() {
        return new Object[]{id, name, type, priority, status, projectName, reportedBy};
    }
}

// ---------------------- FileHandler ----------------------
class FileHandler {
    private static final String USERS_FILE = "users.dat";
    private static final String BUGS_FILE = "bugs.dat";

    public static void saveUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File f = new File(USERS_FILE);
        if (!f.exists()) return users;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void saveBugs(List<Bug> bugs) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BUGS_FILE))) {
            oos.writeObject(bugs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Bug> loadBugs() {
        List<Bug> bugs = new ArrayList<>();
        File f = new File(BUGS_FILE);
        if (!f.exists()) return bugs;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BUGS_FILE))) {
            bugs = (List<Bug>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return bugs;
    }
}

// ---------------------- EmailSimulator ----------------------
class EmailSimulator {
    public static void sendEmail(String to, String subject, String body) {
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("==========================");
    }
}

// ---------------------- Main App ----------------------
public class BugTrackingSystem {
    static List<User> users;
    static List<Bug> bugs;
    static User currentUser;

    public static void main(String[] args) {
        // Set Nimbus L&F and fonts
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("Label.font", new FontUIResource("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Button.font", new FontUIResource("Segoe UI", Font.BOLD, 13));
            UIManager.put("Table.font", new FontUIResource("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Table.rowHeight", 24);
        } catch (Exception ignored) {
        }

        users = FileHandler.loadUsers();
        bugs = FileHandler.loadBugs();

        // ensure default admin exists
        boolean adminExists = users.stream().anyMatch(u -> u.getRole() == Role.ADMIN && u.getUsername().equals("admin"));
        if (!adminExists) {
            users.add(new User("admin", "admin123", Role.ADMIN));
            FileHandler.saveUsers(users);
        }

        SwingUtilities.invokeLater(BugTrackingSystem::createLoginGUI);
    }

    // ---------------------- Login GUI ----------------------
    private static void createLoginGUI() {
        JFrame frame = new JFrame("Bug Tracking System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 450);
        frame.setLayout(new BorderLayout());

        // Left panel (logo/info)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(34, 94, 45)); // أخضر غامق
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("Bug Tracking System", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel slogan = new JLabel("Track & Fix Bugs Easily", SwingConstants.CENTER);
        slogan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        slogan.setForeground(new Color(220, 240, 220));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logo);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(slogan);
        leftPanel.add(Box.createVerticalGlue());

        // Right panel (login form inside a card)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(255, 255, 255));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(450, 580));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, false),
                new EmptyBorder(30, 40, 30, 40)
        ));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10 );
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginLabel = new JLabel("Login", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginLabel.setForeground(new Color(34, 94, 45));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(new Color(40, 90, 40));
        JTextField userText = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(new Color(40, 90, 40));
        JPasswordField passText = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(34, 94, 45));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(240, 40));
        loginBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(34, 94, 45), 0, true),
                new EmptyBorder(6, 10, 6, 10)
        ));




        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(Color.WHITE);                 // الخلفية أبيض
        registerBtn.setForeground(new Color(34, 94, 45));       // النص أخضر
        registerBtn.setFocusPainted(false);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setPreferredSize(new Dimension(140, 40));
        registerBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(34, 94, 45), 0, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

// Hover effect
        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(34, 94, 45)); // الخلفية تبقى أخضر
                registerBtn.setForeground(Color.WHITE);           // النص يبقى أبيض
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(Color.WHITE);           // يرجع أبيض
                registerBtn.setForeground(new Color(34, 94, 45)); // النص يرجع أخضر
            }
        });




        // Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(loginLabel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 2;
        card.add(userLabel, gbc);
        gbc.gridy++;
        card.add(userText, gbc);
        gbc.gridy++;
        card.add(passLabel, gbc);
        gbc.gridy++;
        card.add(passText, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        card.add(loginBtn, gbc);
        gbc.gridx = 1;
        card.add(registerBtn, gbc);

        rightPanel.add(card);

        // Add to frame
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);

        // Actions
        loginBtn.addActionListener(e -> {
            String username = userText.getText().trim();
            String password = new String(passText.getPassword());
            Optional<User> userOpt = users.stream()
                    .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                    .findFirst();
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                frame.dispose();
                openDashboard();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password", "Login Failed", JOptionPane.WARNING_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> {
            if ("admin".equals(userText.getText().trim())) {
                new RegistrationDialog(null).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "Only admin can register new users (type 'admin' in username to open)", "Access Denied", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    // ---------------------- Dashboard switch ----------------------
    private static void openDashboard() {
        switch (currentUser.getRole()) {
            case ADMIN -> new AdminDashboard().setVisible(true);
            case TESTER -> new TesterDashboard().setVisible(true);
            case DEVELOPER -> new DeveloperDashboard().setVisible(true);
            case PROJECT_MANAGER -> new ProjectManagerDashboard().setVisible(true);
        }
    }

    // ---------------------- Admin Dashboard ----------------------
    static class AdminDashboard extends JFrame {
        DefaultTableModel userModel;
        JTable userTable;

        public AdminDashboard() {
            super("Admin Dashboard - " + currentUser.getUsername());
            initialize();
        }

        private void initialize() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(900, 520);
            setLayout(new BorderLayout(8, 8));

            JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            add(title, BorderLayout.NORTH);

            String[] cols = {"Username", "Role"};
            userModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            userTable = new JTable(userModel);
            userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            userTable.setRowHeight(24);

            refreshUsers();

            JScrollPane sp = new JScrollPane(userTable);
            sp.setBorder(BorderFactory.createTitledBorder("Registered Users"));
            add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            JButton addBtn = new JButton("Add User");
            JButton editBtn = new JButton("Edit User");
            JButton delBtn = new JButton("Delete User");
            JButton viewBugsBtn = new JButton("View All Bugs");
            JButton logoutBtn = new JButton("Logout");
            bottom.add(addBtn);
            bottom.add(editBtn);
            bottom.add(delBtn);
            bottom.add(viewBugsBtn);
            bottom.add(logoutBtn);
            add(bottom, BorderLayout.SOUTH);

            addBtn.addActionListener(e -> {
                new RegistrationDialog(this).setVisible(true);
                refreshUsers();
            });

            editBtn.addActionListener(e -> {
                int r = userTable.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Select a user to edit");
                    return;
                }
                String username = (String) userModel.getValueAt(r, 0);
                User u = users.stream().filter(x -> x.getUsername().equals(username)).findFirst().orElse(null);
                if (u != null) new EditUserDialog(this, u).setVisible(true);
                refreshUsers();
            });

            delBtn.addActionListener(e -> {
                int r = userTable.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Select a user to delete");
                    return;
                }
                String username = (String) userModel.getValueAt(r, 0);
                if (username.equals(currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "Cannot delete yourself");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    users.removeIf(u -> u.getUsername().equals(username));
                    FileHandler.saveUsers(users);
                    refreshUsers();
                }
            });

            viewBugsBtn.addActionListener(e -> new AdminBugsDialog(this).setVisible(true));

            logoutBtn.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(BugTrackingSystem::createLoginGUI);
            });

            setLocationRelativeTo(null);
        }

        private void refreshUsers() {
            userModel.setRowCount(0);
            for (User u : users) userModel.addRow(new Object[]{u.getUsername(), u.getRole().name()});
        }

        // Edit user dialog (inner)
        class EditUserDialog extends JDialog {
            public EditUserDialog(JFrame parent, User user) {
                super(parent, "Edit User - " + user.getUsername(), true);
                setSize(360, 200);
                setLayout(new GridBagLayout());
                setLocationRelativeTo(parent);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(8, 8, 8, 8);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                JLabel userL = new JLabel("Username:");
                JTextField userF = new JTextField(user.getUsername());
                userF.setEditable(false);
                JLabel passL = new JLabel("Password:");
                JPasswordField passF = new JPasswordField(user.getPassword());
                JLabel roleL = new JLabel("Role:");
                JComboBox<Role> roleBox = new JComboBox<>(Role.values());
                roleBox.setSelectedItem(user.getRole());
                JButton save = new JButton("Save");

                gbc.gridx = 0;
                gbc.gridy = 0;
                add(userL, gbc);
                gbc.gridx = 1;
                add(userF, gbc);
                gbc.gridx = 0;
                gbc.gridy = 1;
                add(passL, gbc);
                gbc.gridx = 1;
                add(passF, gbc);
                gbc.gridx = 0;
                gbc.gridy = 2;
                add(roleL, gbc);
                gbc.gridx = 1;
                add(roleBox, gbc);
                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.gridwidth = 2;
                add(save, gbc);

                save.addActionListener(ev -> {
                    user.setPassword(new String(passF.getPassword()));
                    user.setRole((Role) roleBox.getSelectedItem());
                    FileHandler.saveUsers(users);
                    dispose();
                });
            }
        }

        // Admin view all bugs dialog
        class AdminBugsDialog extends JDialog {
            public AdminBugsDialog(JFrame parent) {
                super(parent, "All Bugs", true);
                setSize(900, 420);
                setLocationRelativeTo(parent);
                String[] cols = {"ID", "Name", "Type", "Priority", "Status", "Project", "Assigned To", "Reported By"};
                DefaultTableModel model = new DefaultTableModel(cols, 0) {
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                };
                JTable table = new JTable(model);
                table.setRowHeight(24);
                for (Bug b : bugs) model.addRow(b.toTableRowForAdmin());
                add(new JScrollPane(table), BorderLayout.CENTER);

                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                JButton close = new JButton("Close");
                p.add(close);
                add(p, BorderLayout.SOUTH);
                close.addActionListener(e -> dispose());
            }
        }
    }

    // ---------------------- Tester Dashboard ----------------------
    static class TesterDashboard extends JFrame {
        DefaultTableModel model;
        JTable table;

        public TesterDashboard() {
            super("Tester Dashboard - " + currentUser.getUsername());
            initialize();
        }

        private void initialize() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(900, 460);
            setLayout(new BorderLayout(8, 8));

            JLabel title = new JLabel("Tester Dashboard", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            add(title, BorderLayout.NORTH);

            String[] cols = {"ID", "Name", "Type", "Priority", "Status", "Project", "Assigned To"};
            model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            table = new JTable(model);
            table.setRowHeight(24);
            refreshTable();

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(BorderFactory.createTitledBorder("My Reported Bugs"));
            add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            JButton report = new JButton("Report Bug");
            JButton refresh = new JButton("Refresh");
            JButton logout = new JButton("Logout");
            bottom.add(report);
            bottom.add(refresh);
            bottom.add(logout);
            add(bottom, BorderLayout.SOUTH);

            report.addActionListener(e -> {
                new BugReportDialog(this).setVisible(true);
                refreshTable();
            });

            refresh.addActionListener(e -> refreshTable());

            logout.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(BugTrackingSystem::createLoginGUI);
            });

            setLocationRelativeTo(null);
        }

        private void refreshTable() {
            model.setRowCount(0);
            for (Bug b : bugs) {
                if (b.getReportedBy().equals(currentUser.getUsername())) model.addRow(b.toTableRowForTester());
            }
        }
    }

    // ---------------------- Developer Dashboard ----------------------
    static class DeveloperDashboard extends JFrame {
        DefaultTableModel model;
        JTable table;

        public DeveloperDashboard() {
            super("Developer Dashboard - " + currentUser.getUsername());
            initialize();
        }

        private void initialize() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(880, 450);
            setLayout(new BorderLayout(8, 8));

            JLabel title = new JLabel("Developer Dashboard", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            add(title, BorderLayout.NORTH);

            String[] cols = {"ID", "Name", "Type", "Priority", "Status", "Project", "Reported By"};
            model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            table = new JTable(model);
            table.setRowHeight(24);
            refreshTable();

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(BorderFactory.createTitledBorder("Assigned Bugs"));
            add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            JButton update = new JButton("Update Status");
            JButton refresh = new JButton("Refresh");
            JButton logout = new JButton("Logout");
            bottom.add(update);
            bottom.add(refresh);
            bottom.add(logout);
            add(bottom, BorderLayout.SOUTH);

            update.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Select a bug to update");
                    return;
                }
                int bugId = (Integer) model.getValueAt(r, 0);
                Bug b = bugs.stream().filter(x -> x.getId() == bugId).findFirst().orElse(null);
                if (b == null) return;
                BugStatus[] statuses = BugStatus.values();
                BugStatus chosen = (BugStatus) JOptionPane.showInputDialog(this, "Select status",
                        "Update Status", JOptionPane.QUESTION_MESSAGE, null, statuses, b.getStatus());
                if (chosen != null) {
                    b.setStatus(chosen);
                    FileHandler.saveBugs(bugs);
                    refreshTable();
                }
            });

            refresh.addActionListener(e -> refreshTable());

            logout.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(BugTrackingSystem::createLoginGUI);
            });

            setLocationRelativeTo(null);
        }

        private void refreshTable() {
            model.setRowCount(0);
            for (Bug b : bugs) {
                if (currentUser.getUsername().equals(b.getAssignedDeveloper()))
                    model.addRow(b.toTableRowForDeveloper());
            }
        }
    }

    // ---------------------- Project Manager Dashboard ----------------------
    static class ProjectManagerDashboard extends JFrame {
        DefaultTableModel model;
        JTable table;

        public ProjectManagerDashboard() {
            super("Project Manager Dashboard - " + currentUser.getUsername());
            initialize();
        }

        private void initialize() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1000, 520);
            setLayout(new BorderLayout(8, 8));

            JLabel title = new JLabel("Project Manager Dashboard", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            add(title, BorderLayout.NORTH);

            String[] cols = {"ID", "Name", "Type", "Priority", "Status", "Project", "Assigned To", "Reported By"};
            model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            table = new JTable(model);
            table.setRowHeight(24);
            refreshTable();

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(BorderFactory.createTitledBorder("All Bugs"));
            add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            JButton assign = new JButton("Assign Developer");
            JButton refresh = new JButton("Refresh");
            JButton logout = new JButton("Logout");
            bottom.add(assign);
            bottom.add(refresh);
            bottom.add(logout);
            add(bottom, BorderLayout.SOUTH);

            assign.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Select a bug to assign");
                    return;
                }
                int bugId = (Integer) model.getValueAt(r, 0);
                Bug b = bugs.stream().filter(x -> x.getId() == bugId).findFirst().orElse(null);
                if (b == null) return;

                // prepare developer list
                List<String> devs = new ArrayList<>();
                for (User u : users) if (u.getRole() == Role.DEVELOPER) devs.add(u.getUsername());
                if (devs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No developers available");
                    return;
                }
                String dev = (String) JOptionPane.showInputDialog(this, "Choose developer", "Assign", JOptionPane.QUESTION_MESSAGE, null, devs.toArray(), devs.get(0));
                if (dev != null) {
                    b.setAssignedDeveloper(dev);
                    FileHandler.saveBugs(bugs);
                    refreshTable();
                    EmailSimulator.sendEmail(dev, "New Bug Assigned", "You were assigned bug: " + b.getName());
                }
            });

            refresh.addActionListener(e -> refreshTable());

            logout.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(BugTrackingSystem::createLoginGUI);
            });

            setLocationRelativeTo(null);
        }

        private void refreshTable() {
            model.setRowCount(0);
            for (Bug b : bugs) model.addRow(b.toTableRowForAdmin());
        }
    }

    // ---------------------- Registration Dialog ----------------------
    static class RegistrationDialog extends JDialog {
        public RegistrationDialog(Frame owner) {
            super(owner, "Register New User", true);
            setSize(380, 240);
            setLayout(new GridBagLayout());
            setLocationRelativeTo(owner);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel userL = new JLabel("Username:");
            JTextField userF = new JTextField();
            JLabel passL = new JLabel("Password:");
            JPasswordField passF = new JPasswordField();
            JLabel roleL = new JLabel("Role:");
            JComboBox<Role> roleBox = new JComboBox<>(Role.values());
            JButton register = new JButton("Register");

            gbc.gridx = 0;
            gbc.gridy = 0;
            add(userL, gbc);
            gbc.gridx = 1;
            add(userF, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(passL, gbc);
            gbc.gridx = 1;
            add(passF, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(roleL, gbc);
            gbc.gridx = 1;
            add(roleBox, gbc);
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            add(register, gbc);

            register.addActionListener(e -> {
                String username = userF.getText().trim();
                String password = new String(passF.getPassword());
                Role role = (Role) roleBox.getSelectedItem();
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Fill username & password");
                    return;
                }
                boolean exists = users.stream().anyMatch(u -> u.getUsername().equals(username));
                if (exists) {
                    JOptionPane.showMessageDialog(this, "Username already exists");
                    return;
                }
                users.add(new User(username, password, role));
                FileHandler.saveUsers(users);
                JOptionPane.showMessageDialog(this, "User registered");
                dispose();
            });
        }
    }

    // ---------------------- Bug Report Dialog ----------------------
    static class BugReportDialog extends JDialog {
        public BugReportDialog(Frame owner) {
            super(owner, "Report Bug", true);
            setSize(520, 380);
            setLayout(new GridBagLayout());
            setLocationRelativeTo(owner);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel nameL = new JLabel("Bug Title:");
            JTextField nameF = new JTextField();
            JLabel typeL = new JLabel("Type:");
            JTextField typeF = new JTextField();
            JLabel priorityL = new JLabel("Priority:");
            JComboBox<BugPriority> priorityBox = new JComboBox<>(BugPriority.values());
            JLabel levelL = new JLabel("Level:");
            JComboBox<BugLevel> levelBox = new JComboBox<>(BugLevel.values());
            JLabel projectL = new JLabel("Project Name:");
            JTextField projectF = new JTextField();
            JLabel assignL = new JLabel("Assign to (dev):");
            JComboBox<String> devBox = new JComboBox<>();
            for (User u : users) if (u.getRole() == Role.DEVELOPER) devBox.addItem(u.getUsername());
            JLabel screenshotL = new JLabel("Screenshot Path:");
            JTextField screenshotF = new JTextField();
            JButton browse = new JButton("Browse");
            JButton submit = new JButton("Submit");

            // layout
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(nameL, gbc);
            gbc.gridx = 1;
            add(nameF, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(typeL, gbc);
            gbc.gridx = 1;
            add(typeF, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(priorityL, gbc);
            gbc.gridx = 1;
            add(priorityBox, gbc);
            gbc.gridx = 0;
            gbc.gridy = 3;
            add(levelL, gbc);
            gbc.gridx = 1;
            add(levelBox, gbc);
            gbc.gridx = 0;
            gbc.gridy = 4;
            add(projectL, gbc);
            gbc.gridx = 1;
            add(projectF, gbc);
            gbc.gridx = 0;
            gbc.gridy = 5;
            add(assignL, gbc);
            gbc.gridx = 1;
            add(devBox, gbc);
            gbc.gridx = 0;
            gbc.gridy = 6;
            add(screenshotL, gbc);
            gbc.gridx = 1;
            add(screenshotF, gbc);
            gbc.gridx = 2;
            add(browse, gbc);
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 3;
            add(submit, gbc);

            browse.addActionListener(e -> {
                JFileChooser jc = new JFileChooser();
                int res = jc.showOpenDialog(this);
                if (res == JFileChooser.APPROVE_OPTION) screenshotF.setText(jc.getSelectedFile().getAbsolutePath());
            });

            submit.addActionListener(e -> {
                String title = nameF.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title required");
                    return;
                }
                int newId = bugs.isEmpty() ? 1 : bugs.get(bugs.size() - 1).getId() + 1;
                BugPriority pr = (BugPriority) priorityBox.getSelectedItem();
                BugLevel lv = (BugLevel) levelBox.getSelectedItem();
                String assigned = devBox.getItemCount() > 0 ? (String) devBox.getSelectedItem() : "Unassigned";
                Bug b = new Bug(newId, title,
                        typeF.getText().trim(),
                        pr, lv,
                        projectF.getText().trim(),
                        new Date(),
                        BugStatus.OPEN,
                        assigned,
                        screenshotF.getText().trim(),
                        currentUser.getUsername());
                bugs.add(b);
                FileHandler.saveBugs(bugs);
                if (!"Unassigned".equals(assigned))
                    EmailSimulator.sendEmail(assigned, "New Bug Assigned", "You were assigned: " + title);
                JOptionPane.showMessageDialog(this, "Bug reported");
                dispose();
            });
        }
    }
}
