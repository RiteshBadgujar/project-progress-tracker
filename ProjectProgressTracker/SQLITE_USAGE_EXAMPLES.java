/**
 * SQLITE REPOSITORY USAGE EXAMPLES
 *
 * This file demonstrates practical examples of using AppRepository
 * in your Android Activities.
 */

// =====================================
// EXAMPLE 1: LOGIN ACTIVITY
// =====================================

/*
public class LoginActivity extends AppCompatActivity {

    private AppRepository repository;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize repository
        repository = new AppRepository(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query database for user
        User user = repository.getUserByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            // Login successful
            Log.d("LOGIN", "User authenticated: " + user.getName());

            // Save session
            SessionManager.setUser(this, user);

            // Navigate to dashboard
            navigateToDashboard(user.getRole());
        } else {
            // Login failed
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            Log.e("LOGIN", "Authentication failed for: " + email);
        }
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("Admin".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("Teacher".equalsIgnoreCase(role)) {
            intent = new Intent(this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
*/

// =====================================
// EXAMPLE 2: GROUP CREATION ACTIVITY
// =====================================

/*
public class GroupSelectionActivity extends AppCompatActivity {

    private AppRepository repository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        // Initialize
        repository = new AppRepository(this);
        currentUser = SessionManager.getUser(this);

        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> showCreateGroupDialog());
    }

    private void showCreateGroupDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_group);

        TextInputEditText etGroupName = dialog.findViewById(R.id.etGroupName);
        TextInputEditText etProjectTitle = dialog.findViewById(R.id.etProjectTitle);
        TextInputEditText etTechnology = dialog.findViewById(R.id.etTechnology);

        dialog.findViewById(R.id.btnFinalCreateGroup).setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String projectTitle = etProjectTitle.getText().toString().trim();
            String technology = etTechnology.getText().toString().trim();

            if (groupName.isEmpty() || projectTitle.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create group object
            Group newGroup = new Group(
                null,  // Auto-generate ID
                groupName,
                projectTitle,
                technology,
                "",  // Guide name
                "",  // Guide email
                currentUser.getClassCode(),
                currentUser.getEmail()
            );

            // Add leader as student
            newGroup.addStudent(currentUser.getEmail());

            // INSERT INTO DATABASE
            boolean success = repository.insertGroup(newGroup);

            if (success) {
                Log.d("GROUP", "Group created: " + newGroup.getGroupId());
                Toast.makeText(this, "Group created! Code: " + newGroup.getGroupId(),
                              Toast.LENGTH_LONG).show();
                dialog.dismiss();
                finish();
            } else {
                Log.e("GROUP", "Failed to create group");
                Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
*/

// =====================================
// EXAMPLE 3: GROUP WORKSPACE (LIST TASKS)
// =====================================

/*
public class GroupWorkspaceActivity extends AppCompatActivity {

    private AppRepository repository;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<TaskModel> taskList;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_workspace);

        // Initialize
        repository = new AppRepository(this);
        groupId = getIntent().getStringExtra("groupId");

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

        // Load tasks from database
        loadTasksFromDatabase();

        // Setup add task button
        findViewById(R.id.btnAddTask).setOnClickListener(v -> showAddTaskDialog());
    }

    /**
     * Load tasks for this group from SQLite database
     */
    private void loadTasksFromDatabase() {
        // QUERY: Get all tasks for this group
        List<TaskModel> dbTasks = repository.getTasksByGroup(groupId);

        // Update the list (same reference used by adapter)
        taskList.clear();
        taskList.addAll(dbTasks);

        // Notify adapter to refresh UI
        adapter.notifyDataSetChanged();

        Log.d("TASKS", "Loaded " + taskList.size() + " tasks for group: " + groupId);
    }

    /**
     * Show dialog to add new task
     */
    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_task);

        TextInputEditText etTaskName = dialog.findViewById(R.id.etTaskName);
        TextInputEditText etDeadline = dialog.findViewById(R.id.etDeadline);

        dialog.findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString().trim();
            String deadline = etDeadline.getText().toString().trim();

            if (taskName.isEmpty()) {
                Toast.makeText(this, "Enter task name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create task
            TaskModel newTask = new TaskModel();
            newTask.setTaskId(UUID.randomUUID().toString());
            newTask.setTaskName(taskName);
            newTask.setGroupId(groupId);
            newTask.setClassCode(currentUser.getClassCode());
            newTask.setDeadline(deadline);
            newTask.setAssignedDate(getCurrentDate());
            newTask.setCompleted(false);

            // INSERT INTO DATABASE
            boolean success = repository.insertTask(newTask);

            if (success) {
                Log.d("TASK", "Task added: " + newTask.getTaskId());

                // REFRESH UI IMMEDIATELY (live update)
                loadTasksFromDatabase();

                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Log.e("TASK", "Failed to add task");
                Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}
*/

// =====================================
// EXAMPLE 4: ADMIN VIEW ALL TASKS
// =====================================

/*
public class AdminDashboardActivity extends AppCompatActivity {

    private AppRepository repository;
    private RecyclerView recyclerView;
    private List<TaskModel> allTasks;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize
        repository = new AppRepository(this);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAllTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allTasks = new ArrayList<>();
        adapter = new TaskAdapter(allTasks);
        recyclerView.setAdapter(adapter);

        // Load all tasks from database
        loadAllTasks();
    }

    /**
     * Load ALL tasks from database (for admin view)
     */
    private void loadAllTasks() {
        // QUERY: Get all tasks from database
        List<TaskModel> dbTasks = repository.getAllTasks();

        // Update list
        allTasks.clear();
        allTasks.addAll(dbTasks);

        // Refresh UI
        adapter.notifyDataSetChanged();

        Log.d("ADMIN", "Loaded " + allTasks.size() + " total tasks");
    }
}
*/

// =====================================
// EXAMPLE 5: REGISTER NEW USER
// =====================================

/*
public class RegisterActivity extends AppCompatActivity {

    private AppRepository repository;
    private TextInputEditText etName, etEmail, etPassword, etRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize
        repository = new AppRepository(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRole = findViewById(R.id.etRole);

        findViewById(R.id.btnRegister).setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = etRole.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user already exists
        User existing = repository.getUserByEmail(email);
        if (existing != null) {
            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new user
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);
        newUser.setClassCode("");

        // INSERT INTO DATABASE
        boolean success = repository.insertUser(newUser);

        if (success) {
            Log.d("REGISTER", "User registered: " + email);
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

            // Navigate to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Log.e("REGISTER", "Registration failed for: " + email);
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}
*/

// =====================================
// EXAMPLE 6: FILTER TASKS BY CLASS
// =====================================

/*
public class ClassTasksFragment extends Fragment {

    private AppRepository repository;
    private RecyclerView recyclerView;
    private List<TaskModel> classTaskList;
    private String classCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_tasks, container, false);

        // Get class code
        classCode = getArguments().getString("classCode");

        // Initialize
        repository = new AppRepository(getContext());

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        classTaskList = new ArrayList<>();
        recyclerView.setAdapter(new TaskAdapter(classTaskList));

        // Load tasks for this class
        loadClassTasks();

        return view;
    }

    /**
     * Load tasks filtered by class code from database
     */
    private void loadClassTasks() {
        // QUERY: Filter by classCode
        List<TaskModel> dbTasks = repository.getTasksByClass(classCode);

        classTaskList.clear();
        classTaskList.addAll(dbTasks);

        recyclerView.getAdapter().notifyDataSetChanged();

        Log.d("CLASS_TASKS", "Loaded " + classTaskList.size()
              + " tasks for class: " + classCode);
    }
}
*/

// =====================================
// EXAMPLE 7: UPDATE TASK COMPLETION
// =====================================

/*
public class TaskDetailActivity extends AppCompatActivity {

    private AppRepository repository;
    private TaskModel currentTask;
    private CheckBox cbCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Initialize
        repository = new AppRepository(this);

        // Get task ID from intent
        String taskId = getIntent().getStringExtra("taskId");

        // QUERY: Fetch specific task
        currentTask = repository.getTaskById(taskId);

        if (currentTask == null) {
            Log.e("TASK", "Task not found: " + taskId);
            finish();
            return;
        }

        // Display task info
        cbCompleted = findViewById(R.id.cbCompleted);
        cbCompleted.setChecked(currentTask.isCompleted());

        cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update in database
            currentTask.setCompleted(isChecked);
            boolean success = repository.updateTask(currentTask);

            if (success) {
                Log.d("TASK", "Task status updated: " + isChecked);
                Toast.makeText(this, "Task status updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
*/

// =====================================
// KEY PATTERNS TO REMEMBER
// =====================================

/**
 * PATTERN 1: INSERT AND REFRESH UI
 *
 * // Insert data
 * repository.insertTask(task);
 *
 * // Refresh list immediately
 * taskList.clear();
 * taskList.addAll(repository.getTasksByGroup(groupId));
 * adapter.notifyDataSetChanged();
 */

/**
 * PATTERN 2: FETCH AND DISPLAY
 *
 * // Fetch from database
 * List<TaskModel> tasks = repository.getTasksByClass(classCode);
 *
 * // Update adapter
 * taskList.addAll(tasks);
 * adapter.notifyDataSetChanged();
 */

/**
 * PATTERN 3: UPDATE AND REFRESH
 *
 * // Get item
 * TaskModel task = repository.getTaskById(taskId);
 *
 * // Modify
 * task.setCompleted(true);
 *
 * // Update in database
 * repository.updateTask(task);
 *
 * // Refresh UI
 * loadDataFromDatabase();
 */

/**
 * PATTERN 4: DELETE AND REFRESH
 *
 * // Delete from database
 * repository.deleteTask(taskId);
 *
 * // Refresh list
 * taskList.clear();
 * taskList.addAll(repository.getAllTasks());
 * adapter.notifyDataSetChanged();
 */

