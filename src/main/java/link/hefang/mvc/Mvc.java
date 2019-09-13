package link.hefang.mvc;

import link.hefang.enums.LogLevel;
import link.hefang.helpers.*;
import link.hefang.interfaces.ICache;
import link.hefang.interfaces.ILogger;
import link.hefang.mvc.annotations.Action;
import link.hefang.mvc.annotations.Controller;
import link.hefang.mvc.annotations.NeedLogin;
import link.hefang.mvc.caches.SimpleCache;
import link.hefang.mvc.controllers.BaseController;
import link.hefang.mvc.databases.BaseDB;
import link.hefang.mvc.entities.PostFile;
import link.hefang.mvc.entities.Router;
import link.hefang.mvc.enums.AuthType;
import link.hefang.mvc.exceptions.ActionNotFoundException;
import link.hefang.mvc.exceptions.ControllerNotFoundException;
import link.hefang.mvc.exceptions.NotEnableDatabaseException;
import link.hefang.mvc.exceptions.PackageInvalidException;
import link.hefang.mvc.interfaces.IApplication;
import link.hefang.mvc.logger.ConsoleLogger;
import link.hefang.mvc.models.BaseLoginModel;
import link.hefang.mvc.views.BaseView;
import link.hefang.mvc.views.FileView;
import link.hefang.mvc.views.TextView;
import link.hefang.network.RequestMethod;
import link.hefang.string.Charsets;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;

import static link.hefang.helpers.CollectionHelper.*;
import static link.hefang.helpers.ParseHelper.*;
import static link.hefang.helpers.StringHelper.*;
import static link.hefang.mvc.models.BaseLoginModel.LOGIN_SESSION_KEY;

@WebServlet(name = "Mvc", loadOnStartup = Mvc.FRAMEWORK_LOAD_ON_START_UP, urlPatterns = {"/"})
final public class Mvc extends HttpServlet {
    public static final int FRAMEWORK_LOAD_ON_START_UP = 10;
    public static final String VERSION = "1.0.0";
    public static final String classPath = Thread.currentThread().getContextClassLoader().getResource("").getFile();
    public static final String webInfPath = classPath.replace(File.separatorChar + "classes" + File.separatorChar, "");
    public static final String appRootPath = webInfPath.replace(File.separatorChar + "WEB-INF", "");

    private static Map<String, Object> globalConfig = hashMapOf();

    @NotNull
    private static Properties properties = new Properties();

    @NotNull
    private static String urlRoot = "";

    private static String[] developers = new String[0];

    @NotNull
    private static String fileUrlPrefix = "";
    @NotNull
    private static String fileSavePath = "";

    @NotNull
    private static String projectPackage = "";
    @NotNull
    private static String defaultModule = "main";
    @NotNull
    private static String defaultController = "home";
    @NotNull
    private static String defaultAction = "index";
    @NotNull
    private static Charset defaultCharset = Charsets.UTF_8;
    private static int defaultPgSize = 20;

    @Nullable
    private static String passwordSalt = null;
    @NotNull
    private static AuthType authType = AuthType.SESSION;

    @Nullable
    private static BaseDB database;

    private static HashMap<String, Class<? extends BaseController>> controllers = hashMapOf();
    private static HashMap<String, Method> actions = hashMapOf();

    @NotNull
    private static IApplication application = new SimpleApplication();

    @NotNull
    private static ILogger logger = new ConsoleLogger();

    @NotNull
    private static ICache cache = new SimpleCache();

    private static boolean debug = false;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        globalConfig = application.onInit(config);
        if (globalConfig.isEmpty()) {
            Mvc.logger.warn("未读取到配置�?", "Mvc.getConfig方法将无法返回配置�??", null);
        } else {
            Mvc.logger.debug("共读取到${configs.size}个配置项",
                    joinToString(map(globalConfig, item -> item.getKey() + ":" + item.getValue()), "\n"));
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BaseView view = null;
        try {
            super.service(req, resp);
            req.setCharacterEncoding(defaultCharset.displayName());
            resp.setCharacterEncoding(defaultCharset.displayName());

            Router router = application.onRequest(req);

            if (router != null) {
                view = handleRequest(router, req, resp);
            }

            router = Router.parse(req);

            if (view == null) {
                view = handleThemeStaticResources(router, req);
            }

            if (view == null) {
                view = handleStaticResources(req);
            }

            if (view == null) {
                view = handleRequest(router, req, resp);
            }

        } catch (Throwable e) {
            view = application.onException(e);
        }
        (view == null ? new TextView("") : view).compile().render(resp);
    }

    //理静态资�?
    @Nullable
    private BaseView handleStaticResources(@NotNull HttpServletRequest req) {
        final String uri = req.getRequestURI();
        if (startsWith(uri, true, "/WEB-INF", "/META-INF")) {
            return null;
        }
        File file = new File(appRootPath + urlDecode(uri).replace('/', File.separatorChar));
        if (!file.exists() || file.isDirectory()) return null;
        return new FileView(file);
    }

    //处理主题包内的静态资�?
    @Nullable
    private BaseView handleThemeStaticResources(@NotNull Router router, @NotNull HttpServletRequest req) {
        final String uri = req.getRequestURI();
        if (!startsWith(uri, true, "/theme/resources/" + router.getTheme() + "/") ||
                endsWith(uri, true, ".jsp")) return null;
        File file = new File(appRootPath + urlDecode(uri)
                .replace('/', File.separatorChar)
                .replace("/theme/resources", "/WEB-INF/views"));
        if (!file.exists() || file.isDirectory()) return null;
        return new FileView(file, FileHelper.mimeType(file));
    }

    //处理请求
    @Nullable
    private BaseView handleRequest(@NotNull Router router, @NotNull HttpServletRequest req, @NotNull HttpServletResponse resp)
            throws Exception {
        BaseController controller = null;
        final String ck = router.getModule() + "/" + router.getController();
        final String ak = ck + "/" + router.getAction() + "/" + isNullOrBlank(router.getCmd());
        if (!controllers.containsKey(ck)) {
            resp.setStatus(404);
            throw new ControllerNotFoundException(router);
        }
        if (!actions.containsKey(ck)) {
            resp.setStatus(404);
            throw new ActionNotFoundException(router);
        }
        Class<? extends BaseController> cc = controllers.get(ck);
        Method am = actions.get(ak);
        controller = cc.newInstance();

        final Field request = cc.getDeclaredField("request");
        request.setAccessible(true);
        request.set(controller, req);

        final Field response = cc.getDeclaredField("response");
        response.setAccessible(true);
        response.set(controller, resp);

        BaseView view = checkLogin(controller, am);
        if (view != null) {
            return view;
        }

        checkMethod(am, req.getMethod());

        final Field gets = cc.getDeclaredField("gets");
        gets.setAccessible(true);
        gets.set(controller, ParseHelper.queryString(req.getQueryString(), true));

        if (RequestMethod.POST.name().equalsIgnoreCase(req.getMethod())) {
            parsePosts(req, controller);
        }

        final Field routerFiled = cc.getDeclaredField("router");
        routerFiled.setAccessible(true);
        routerFiled.set(controller, router);

        view = (BaseView) (isNullOrBlank(router.getCmd()) ? am.invoke(controller) : am.invoke(controller, router.getCmd()));

        //流程执行结束后删除上传文件的临时文件
        try {
            final Field filesFiled = cc.getDeclaredField("files");
            filesFiled.setAccessible(true);
            //noinspection unchecked
            final HashMap<String, PostFile> files = (HashMap<String, PostFile>) filesFiled.get(controller);
            files.values().forEach(PostFile::delete);
        } catch (Throwable ignored) {
        }
        return view;
    }

    //解析post数据
    private void parsePosts(@NotNull HttpServletRequest req, BaseController controller)
            throws NoSuchFieldException, IOException, IllegalAccessException, FileUploadException {
        final String contentType = req.getContentType();
        if (contains(contentType, true, "x-www-form-urlencoded")) {
            final Field posts = controller.getClass().getDeclaredField("posts");
            posts.setAccessible(true);
            posts.set(controller, ParseHelper.queryString(IOHelper.readText(req.getInputStream()), true));
        } else if (contains(contentType, true, "multipart/")) {
            parseMultiPart(req, controller);
        }
    }

    //解析上传的文�?
    @SuppressWarnings("unchecked")
    private void parseMultiPart(HttpServletRequest req, @NotNull BaseController controller)
            throws FileUploadException, NoSuchFieldException, IllegalAccessException {
        final DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(PostFile.path);
        final ServletFileUpload upload = new ServletFileUpload(factory);
        final List<FileItem> items = upload.parseRequest(new ServletRequestContext(req));

        final Class<? extends BaseController> controllerClass = controller.getClass();

        final Field filesField = controllerClass.getDeclaredField("files");
        filesField.setAccessible(true);
        final HashMap<String, PostFile> files = (HashMap<String, PostFile>) filesField.get(controller);

        final Field postsField = controllerClass.getDeclaredField("posts");
        postsField.setAccessible(true);
        final HashMap<String, String> posts = (HashMap<String, String>) postsField.get(controller);


        for (FileItem it : items) {
            if (it.isFormField()) {
                posts.put(it.getFieldName(), it.getString());
            } else {
                files.put(it.getFieldName(), new PostFile(it.get(), it.getName(), it.getFieldName(), it.getContentType()));
            }
        }

        filesField.set(controller, files);
        postsField.set(controller, posts);
    }

    //�?查请求方�?
    private void checkMethod(@NotNull Method am, @NotNull String method) throws ActionNotFoundException {
        final Action action = am.getAnnotation(Action.class);
        if (action == null || action.method().equals(RequestMethod.DEFAULT)) return;
        if (!method.equalsIgnoreCase(action.method().name())) {
            throw new ActionNotFoundException("Request method NOT match!");
        }
    }

    //�?查是否有用户登录和用户访问权�?
    @Contract("null, _ -> null")
    private BaseView checkLogin(@Nullable BaseController controller, @NotNull Method actionMethod) {
        if (controller == null) return null;
        NeedLogin controllerLogin = controller.getClass().getAnnotation(NeedLogin.class);
        NeedLogin actionLogin = actionMethod.getAnnotation(NeedLogin.class);
        Object cache = null;
        if (Mvc.getAuthType() == AuthType.SESSION) {
            cache = controller._session(LOGIN_SESSION_KEY);
        } else if (Mvc.getAuthType() == AuthType.TOKEN) {
            cache = Mvc.cache.get(LOGIN_SESSION_KEY);
        }

        if (cache instanceof BaseLoginModel) {
            try {
                final Field login = controller.getClass().getDeclaredField("login");
                login.setAccessible(true);
                login.set(controller, cache);
            } catch (Exception ignored) {
            }
        }

        BaseView view = checkUserPermission(controller, controllerLogin);
        if (view == null) {
            view = checkUserPermission(controller, actionLogin);
        }

        return view;
    }

    //根据控制器和动作的注解检查权�?
    @Nullable
    @Contract("null, _ -> null; !null, null -> null")
    private BaseView checkUserPermission(@Nullable BaseController controller, @Nullable NeedLogin needLogin) {
        if (controller == null || needLogin == null || !needLogin.value()) return null;
        final BaseLoginModel login = controller.getLogin();
        //noinspection ConstantConditions
        if (login == null) {
            return controller._needLogin(needLogin.needLoginMessage());
        }

        if (login.isLockedScreen() && needLogin.needUnLock()) {
            return controller._needUnlock(needLogin.needUnLockMessage());
        }

        if (!login.isAdmin() && needLogin.needAdmin()) {
            return controller._needAdmin(needLogin.needAdminMessage());
        }

        if (!login.isSuperAdmin() && needLogin.needSuperAdmin()) {
            return controller._needSuperAdmin(needLogin.needSuperAdminMessage());
        }

        if (isNotEmpty(needLogin.needRoles()) && !inArray(needLogin.needRoles(), login.getRoleID())) {
            return controller._needPermission(needLogin.needRolesMessage());
        }

        if (!login.isDeveloper() && needLogin.needDeveloper()) {
            return controller._needDeveloper(needLogin.needDeveloperMessage());
        }

        return null;
    }

    @Contract(pure = true)
    public static boolean isDebug() {
        return debug;
    }

    @Contract(pure = true)
    @NotNull
    public static ILogger getLogger() {
        return logger;
    }

    @Contract(pure = true)
    @NotNull
    public static String getFileUrlPrefix() {
        return fileUrlPrefix;
    }

    @Contract(pure = true)
    @NotNull
    public static String getFileSavePath() {
        return fileSavePath;
    }

    @Contract(pure = true)
    @NotNull
    public static ICache getCache() {
        return cache;
    }

    @Contract(pure = true)
    @NotNull
    public static String getDefaultModule() {
        return defaultModule;
    }

    private static void setDefaultModule(@NotNull String defaultModule) {
        Mvc.defaultModule = defaultModule;
    }

    @Contract(pure = true)
    @NotNull
    public static String getDefaultController() {
        return defaultController;
    }

    private static void setDefaultController(@NotNull String defaultController) {
        Mvc.defaultController = defaultController;
    }

    @Contract(pure = true)
    @NotNull
    public static String getDefaultAction() {
        return defaultAction;
    }

    private static void setDefaultAction(@NotNull String defaultAction) {
        Mvc.defaultAction = defaultAction;
    }

    @Contract(pure = true)
    @NotNull
    public static Charset getDefaultCharset() {
        return defaultCharset;
    }

    @Contract(pure = true)
    @NotNull
    public static IApplication getApplication() {
        return application;
    }

    @Contract(pure = true)
    public static int getDefaultPgSize() {
        return defaultPgSize;
    }

    @Nullable
    public static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Contract(pure = true)
    @NotNull
    public static String getProjectPackage() {
        return projectPackage;
    }

    @NotNull
    @Contract(pure = true)
    public static BaseDB getDatabase() {
        if (database == null) {
            throw new NotEnableDatabaseException();
        }
        return database;
    }

    @Contract(pure = true)
    @Nullable
    public static String getPasswordSalt() {
        return passwordSalt;
    }

    @Contract(pure = true)
    @NotNull
    public static AuthType getAuthType() {
        return authType;
    }

    @Contract(pure = true)
    public static String[] getDevelopers() {
        return developers;
    }

    @Contract(pure = true)
    @NotNull
    public static String getUrlRoot() {
        return urlRoot;
    }

    @Nullable
    @Contract("_, !null -> !null")
    public static String getProperty(@NotNull String name, @Nullable String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    @Nullable
    public static String getProperty(@NotNull String name) {
        return properties.getProperty(name);
    }

    @Nullable
    @Contract("_, !null -> !null")
    @SuppressWarnings("unchecked")
    public static <T> T getConfig(@NotNull String name, @Nullable T defaultValue) {
        try {
            return (T) globalConfig.getOrDefault(name, defaultValue);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getConfig(@NotNull String name) {
        try {
            final Object value = globalConfig.getOrDefault(name, null);
            return value == null ? null : (T) value;
        } catch (Throwable e) {
            return null;
        }
    }

    static {
        printSystemInfo();
        initProperties();
        initLogger();
        initProject();
        initDatabase();
        initApplication();
        initControllers();
        initAuthorization();
        initUrl();
        initDevelopers();
        initFileUpload();
    }

    private static void initControllers() {
        final String[] controllerPackages = filter(
                getProperty("ext.controller.package", "").split(","),
                StringHelper::isNullOrBlank);
        logger.notice("正在�?" + controllerPackages.length + "包中读取控制�?",
                "\n" + joinToString(controllerPackages, "\n") + "\n");
        final File[] classLibs = FileHelper.listFiles(
                new File(new File(classPath).getParentFile().getAbsolutePath() + SystemHelper.fileSeparator() + "lib")
        );
        final ClassHelper classHelper = new ClassHelper(
                classPath + File.pathSeparatorChar + joinToString(classLibs, File.pathSeparator)
        );
        final ArrayList<Class<? extends BaseController>> controllerClasses = classHelper.findSubClassOf(BaseController.class);
        for (Class<? extends BaseController> it : controllerClasses) {
            final Controller controller = it.getAnnotation(Controller.class);
            String module = it.getPackage().getName();
            String name = it.getSimpleName();
            if (controller == null && (!module.contains(".controllers") || !name.endsWith("Controller"))) {
                continue;
            }
            name = name.replace("Controller", "");
            module = module.replace(".controllers", "").substring(module.lastIndexOf('.') + 1, module.length());

            if (controller != null) {
                if (!controller.isController()) continue;
                name = isNullOrBlank(controller.value()) ? name : controller.value();
                module = isNullOrBlank(controller.module()) ? module : controller.module();
            }
            final String key = (module + "/" + name).toLowerCase();
            if (controllers.containsKey(key)) {
                logger.warn("发现相关模块和名称的控制�?",
                        "已使用已" + it.getName() + "替代" + controllers.get(key).getName(), null);
            }
            controllers.put(key, it);
            initActions(key, it);
        }
        logger.notice("控制器读取完�?", "共读取到" + controllers.size() + "个控制器");
        logger.debug("控制器列�?", joinToString(map(controllers, item -> item + ":" + item.getValue().getName()), "\n"));
    }

    private static void initActions(@NotNull String key, @NotNull Class<? extends BaseController> controller) {
        final Method[] methods = controller.getMethods();
        for (Method it : methods) {
            if (!BaseView.class.isAssignableFrom(it.getReturnType()) ||
                    it.getParameterCount() > 1 ||
                    (it.getParameterTypes().length == 1 && String.class.equals(it.getParameterTypes()[0]))) continue;
            final Action action = it.getAnnotation(Action.class);
            String name = it.getName();
            if (action != null) {
                if (!action.isAction()) continue;
                if (!isNullOrBlank(action.value())) {
                    name = action.value();
                }
            }
            actions.put((key + "/" + name + (it.getParameterCount() == 1)).toLowerCase(), it);
        }
    }

    private static void initFileUpload() {
        fileSavePath = getProperty("file.save.path", "");
        if (isNullOrBlank(fileSavePath)) {
            logger.warn("文件上传", "上传文件保存路径未设�?", null);
            return;
        }

        final File file = new File(fileSavePath);

        if (!file.isDirectory()) {
            logger.warn("文件上传", "上传文件保存路径\"" + fileSavePath + "\"不存在或不是目录", null);
            return;
        }

        if (!file.canWrite()) {
            logger.warn("文件上传", "上传文件保存路径\"" + fileSavePath + "\"不可�?", null);
            return;
        }

        fileUrlPrefix = getProperty("file.url.prefix", "");

        if (isNullOrBlank(fileUrlPrefix)) {
            logger.warn("文件上传", "上传文件访问路径前缀未设�?", null);
        }
    }

    private static void initDevelopers() {
        File file = new File(appRootPath + File.separator + "WEB-INF" + File.separator + "developers.txt");
        if (file.isFile()) {
            logger.notice("�?发�??", "发现�?发�?�列表文�?, 正在读取");
            if (!file.canRead()) {
                logger.warn("�?发�??", "�?发�?�文件不可读, 已忽�?", null);
                return;
            }
            developers = filter(map(IOHelper.readLines(file), String::trim), line -> !startsWith(line, "//", "#")).toArray(new String[0]);
        }
    }

    private static void initUrl() {
        urlRoot = getProperty("url.root", urlRoot);
    }

    private static void initAuthorization() {
        passwordSalt = getProperty("password.salt", "");
        authType = AuthType.valueOf(getProperty("authorization.type", authType.name()));
        logger.notice("授权方式", "当前授权方式�?: " + authType.name());
    }

    private static void initApplication() {
        String appClassName = getProperty("project.application.class");
        if (isNullOrBlank(appClassName)) {
            appClassName = projectPackage + ".Application";
        }
        try {
            final Class<?> appClass = Class.forName(appClassName);
            if (!IApplication.class.isAssignableFrom(appClass)) {
                logger.error("应用", "应用类\"" + appClassName + "\"不是IApplication的实现类", null);
                return;
            }
            application = (IApplication) appClass.newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn("应用", "应用类\"" + appClassName + "\"不存�?", e);
        } catch (IllegalAccessException | InstantiationException e) {
            logger.warn("应用", "应用类\"" + appClassName + "\"实例化时出现异常", e);
        }

    }

    private static void initDatabase() {
        final boolean dbEnable = parseBoolean(getProperty("database.enable"));
        if (!dbEnable) {
            System.err.println("数据库功能未启用");
            return;
        }
        final String dbClassName = getProperty("database.class");
        final String dbDriverName = getProperty("database.driver");
        final String dbPassword = getProperty("database.password");
        final String dbUsername = getProperty("database.username");
        final String dbUrl = getProperty("database.url");
        final String dbCharset = getProperty("database.charset");

        final int dsMaxIdle = parseInt(getProperty("datasource.max.idle"), 10);
        final int dsMinIdle = parseInt(getProperty("datasource.min.idle"), 0);
        final int dsMaxTotal = parseInt(getProperty("datasource.max.total"), 0);
        final long dsMaxWaitMillis = parseLong(getProperty("datasource.max.wait.millis"), 20000);
        logger.notice("数据�?", StringHelper.contact("",
                "\n\t类名:", dbClassName,
                "\n\t驱动:", dbDriverName,
                "\n\t用户�?:", dbUsername,
                "\n\t连接:", dbUrl,
                "\n\t编码:", dbCharset
        ));
        try {
            final Class<?> dbClass = Class.forName(dbClassName);
            if (!BaseDB.class.isAssignableFrom(dbClass)) {
                logger.error("数据�?", "类\"" + dbClassName + "\"不是BaseDB的实现类, 数据库功能已关闭", null);
                return;
            }
            final BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(dbDriverName);
            ds.setUsername(dbUsername);
            ds.setPassword(dbPassword);
            ds.setUrl(dbUrl);

            ds.setMaxIdle(dsMaxIdle);
            ds.setMinIdle(dsMinIdle);
            ds.setMaxTotal(dsMaxTotal);
            ds.setMaxWaitMillis(dsMaxWaitMillis);

            final Constructor<?> constructor = dbClass.getConstructor(DataSource.class);
            database = (BaseDB) constructor.newInstance(ds);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.error("数据�?", "类\"" + dbClassName + "\"未找�?, 数据库功能已关闭", e);
        } catch (Exception e) {
            logger.error("数据�?", "类\"" + dbClassName + "\"实例化时出现异常, 数据库功能已关闭", e);
        }
    }

    private static void initProject() {
        final String pkg = getProperty("project.package");
        if (isNullOrBlank(pkg)) {
            throw new PackageInvalidException();
        }
        projectPackage = pkg;
        defaultModule = getProperty("default.module", defaultModule);
        defaultController = getProperty("default.controller", defaultController);
        defaultAction = getProperty("default.action", defaultAction);
        defaultPgSize = parseInt(getProperty("default.page.size"), defaultPgSize);
        logger.notice("默认默认�?", "当前项目主包�?:" + pkg);

    }

    private static void initLogger() {
        final String loggerClass = getProperty("logger.class");
        if (isNullOrBlank(loggerClass)) {
            return;
        }
        try {
            final Class<?> aClass = Class.forName(loggerClass);
            if (!ILogger.class.isAssignableFrom(aClass)) {
                System.err.print("日志类\"" + loggerClass + "\"不是ILogger的实现类");
                return;
            }
            logger = (ILogger) aClass.newInstance();
            logger.setLevel(debug ? LogLevel.ALL : LogLevel.valueOf(getProperty("logger.level", "WARN")));
        } catch (IllegalAccessException e) {
            System.err.print("日志类\"" + loggerClass + "\"实例化时抛出异常");
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.err.print("日志类\"" + loggerClass + "\"实例化时出错");
        } catch (ClassNotFoundException e) {
            System.err.print("日志类\"" + loggerClass + "\"找不�?");
        }

    }

    private static void printSystemInfo() {
        System.out.println(StringHelper.contact("\n",
                "-----------------------------[ java-mvc ]------------------------------\n\n",
                "        _                                                      \n",
                "       (_)                                                     \n",
                "        _   __ _ __   __  __ _  ______  _ __ ___  __   __  ___ \n",
                "       | | / _` |\\ \\ / / / _` ||______|| '_ ` _ \\ \\ \\ / / / __|\n",
                "       | || (_| | \\ V / | (_| |        | | | | | | \\ V / | (__ \n",
                "       | | \\__,_|  \\_/   \\__,_|        |_| |_| |_|  \\_/   \\___|\n",
                "      _/ |                                                     \n",
                "     |__/                                                      \n",
                "\n\n",
                "-----------------------------[ 系统信息 ]------------------------------\n\n",
                "\n启动时间:", TimeHelper.format(TimeHelper.now()),
                "\n操作系统:", SystemHelper.osName() + SystemHelper.osVersion() + "(" + SystemHelper.osArch() + ")",
                "\n服务器IP:", getHostAddress(),
                "\nJava环境:", SystemHelper.javaVmVendor() + SystemHelper.javaVmName() + SystemHelper.javaVersion(),
                "\n框架版本:", VERSION,
                "\n作   者: 何方",
                "\n教程地址: https://hefang.link",
                "\n帮助文档: https://hefang.link/docs/java-mvc/", VERSION,
                "\n类路径:", classPath,
                "\n应用目录:", appRootPath,
                "\nWEB-INF:", webInfPath + "\n",
                "-----------------------------[ 系统信息 ]------------------------------"
        ));
    }

    private static void initProperties() {
        final File propertiesFile = new File(classPath + File.separatorChar + "config.properties");
        if (propertiesFile.exists()) {
            System.out.println("发现配置文件, 正在读取...\n");
            try {
                properties.load(new FileReader(propertiesFile));
            } catch (Throwable e) {
                System.err.println("读取配置文件时出�?: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        } else {
            System.err.println("配置文件\"" + propertiesFile.getAbsolutePath() + "\"不存�?");
        }
        debug = parseBoolean(getProperty("debug.enable"), false);
    }
}