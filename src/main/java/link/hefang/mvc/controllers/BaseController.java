package link.hefang.mvc.controllers;

import link.hefang.mvc.annotations.Action;
import link.hefang.mvc.entities.ApiResult;
import link.hefang.mvc.entities.PostFile;
import link.hefang.mvc.entities.Router;
import link.hefang.mvc.models.BaseLoginModel;
import link.hefang.mvc.views.BaseView;
import link.hefang.mvc.views.JsonView;
import link.hefang.mvc.views.TextView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

import static link.hefang.helpers.StringHelper.isNullOrBlank;

abstract public class BaseController {
    @Nullable
    private HttpServletRequest request;
    @Nullable
    private HttpServletResponse response;
    private Router router;
    private HashMap<String, String> gets = new HashMap<>();
    private HashMap<String, String> posts = new HashMap<>();
    private HashMap<String, PostFile> files = new HashMap<>();
    private BaseLoginModel login = null;

    @NotNull
    public BaseLoginModel getLogin() {
        return login;
    }

    @Nullable
    public String _get(@NotNull String name) {
        return _get(name, null);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public String _get(@NotNull String name, @Nullable String defaultValue) {
        return gets.getOrDefault(name, defaultValue);
    }

    @Nullable
    public String _post(@NotNull String name) {
        return _post(name, null);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public String _post(@NotNull String name, @Nullable String defaultValue) {
        return posts.getOrDefault(name, defaultValue);
    }

    @Nullable
    public String _request(@NotNull String name) {
        return _request(name, null);
    }

    /**
     * 按照GET,POST,COOKIE的顺序获取前端传过来的值
     *
     * @param name         String 名称
     * @param defaultValue String 默认值
     * @return 值, 若不存在该值返回默认值
     */
    @Nullable
    @Contract("_, !null -> !null")
    public String _request(@NotNull String name, @Nullable String defaultValue) {
        return _get(name, _post(name, _cookie(name, defaultValue)));
    }

    /**
     * 获取客户端ip
     *
     * @return 客户端ip
     */
    @NotNull
    public String _ip() {
        if (request == null) return "";
        String ip = request.getRemoteHost();
        if (isNullOrBlank(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip == null ? "" : ip;
    }

    @Nullable
    public String _userAgent() {
        return _header("User-Agent");
    }

    @Nullable
    public String _header(@NotNull String name) {
        if (request == null) return null;
        return request.getHeader(name);
    }


    @Nullable
    @Contract("_, !null -> !null")
    public String _header(@NotNull String name, @Nullable String defaultValue) {
        if (request == null) return defaultValue;
        String header = request.getHeader(name);
        return header == null ? defaultValue : header;
    }

    @Nullable
    public String _cookie(@NotNull String name) {
        return _cookie(name, null);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public String _cookie(@NotNull String name, @Nullable String defaultValue) {
        if (request == null) return defaultValue;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return defaultValue;
    }

    @NotNull
    public BaseController _addCookie(@NotNull String name, @NotNull String value) {
        _addCookie(new Cookie(name, value));
        return this;
    }

    @NotNull
    public BaseController _addCookie(@NotNull Cookie cookie) {
        if (response != null) {
            response.addCookie(cookie);
        }
        return this;
    }

    @Nullable
    public Object _session(@NotNull String name) {
        return _session(name, null);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Object _session(@NotNull String name, @Nullable Object defaultValue) {
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return defaultValue;
            }
            Object value = session.getAttribute(name);
            return value == null ? defaultValue : value;
        }
        return defaultValue;
    }

    @NotNull
    public BaseController _setSession(@NotNull String name, @Nullable Object value) {
        if (request != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute(name, value);
        }
        return this;
    }

    /**
     * 删除指定 session, 并返回原值
     *
     * @param name session 名称
     * @return 原值, 若session中不存在该值, 返回<code>null</>
     */
    @Nullable
    public Object _removeSession(@NotNull String name) {
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session == null) return null;
            Object value = session.getAttribute(name);
            session.removeAttribute(name);
            return value;
        }
        return null;
    }

    @NotNull
    public BaseController _addHeader(@NotNull String name, @NotNull String value) {
        if (response == null) return this;
        response.addHeader(name, value);
        return this;
    }

    @NotNull
    public BaseController _addHeader(@NotNull String name, long date) {
        if (response == null) return this;
        response.addDateHeader(name, date);
        return this;
    }

    @NotNull
    public BaseController _addHeader(@NotNull String name, int value) {
        if (response == null) return this;
        response.addIntHeader(name, value);
        return this;
    }

    @NotNull
    public BaseController _setHeader(@NotNull String name, @NotNull String value) {
        if (response == null) return this;
        response.setHeader(name, value);
        return this;
    }

    @NotNull
    public BaseController _setHeader(@NotNull String name, long date) {
        if (response == null) return this;
        response.setDateHeader(name, date);
        return this;
    }

    @NotNull
    public BaseController _setHeader(@NotNull String name, int value) {
        if (response == null) return this;
        response.setIntHeader(name, value);
        return this;
    }

    @NotNull
    @Action(isAction = false)
    public BaseView _text(@NotNull String text, @NotNull String contentType) {
        return new TextView(text, contentType);
    }

    @NotNull
    @Action(isAction = false)
    public BaseView _text(@NotNull String text) {
        return new TextView(text);
    }

    @NotNull
    @Action(isAction = false)
    public BaseView _api(@NotNull ApiResult result) {
        return new JsonView(result);
    }

    @NotNull
    @Action(isAction = false)
    public BaseView _apiSuccess(@NotNull Object result) {
        final ApiResult apiResult = new ApiResult();
        apiResult.setSuccess(true).setResult(result);
        return _api(apiResult);
    }

    @NotNull
    @Action(isAction = false)
    public BaseView _apiSuccess() {
        return _apiSuccess("ok");
    }

    @NotNull
    public BaseView _apiFailed(@NotNull String reason
            , boolean needLogin
            , boolean needUnlock
            , boolean needSuperAdmin
            , boolean needPassword
            , boolean needAdmin
            , boolean needPermission
            , boolean needDeveloper) {
        final ApiResult apiResult = new ApiResult();
        apiResult.setSuccess(false)
                .setNeedUnlock(needUnlock)
                .setNeedLogin(needLogin)
                .setNeedSuperAdmin(needSuperAdmin)
                .setNeedPassword(needPassword)
                .setNeedAdmin(needAdmin)
                .setNeedPermission(needPermission)
                .setNeedDeveloper(needDeveloper);
        return _api(apiResult);
    }

    @NotNull
    public BaseView _apiFailed(@NotNull String reason
            , boolean needLogin
            , boolean needUnlock
            , boolean needSuperAdmin
            , boolean needPassword
            , boolean needAdmin
            , boolean needPermission) {
        return _apiFailed(reason, needLogin, needUnlock, needSuperAdmin, needPassword, needAdmin, needPermission, false);
    }

    @NotNull
    public BaseView _apiFailed(@NotNull String reason
            , boolean needLogin
            , boolean needUnlock
            , boolean needSuperAdmin
            , boolean needPassword
            , boolean needAdmin) {
        return _apiFailed(reason, needLogin, needUnlock, needSuperAdmin, needPassword, needAdmin, false);
    }


    @NotNull
    public BaseView _apiFailed(@NotNull String reason
            , boolean needLogin
            , boolean needUnlock
            , boolean needSuperAdmin
            , boolean needPassword) {
        return _apiFailed(reason, needLogin, needUnlock, needSuperAdmin, needPassword, false);
    }


    @NotNull
    public BaseView _apiFailed(@NotNull String reason
            , boolean needLogin
            , boolean needUnlock
            , boolean needSuperAdmin) {
        return _apiFailed(reason, needLogin, needUnlock, needSuperAdmin, false);
    }

    @NotNull
    public BaseView _apiFailed(@NotNull String reason
            , boolean needLogin, boolean needUnlock) {
        return _apiFailed(reason, needLogin, needUnlock, false);
    }

    @NotNull
    public BaseView _apiFailed(@NotNull String reason, boolean needLogin) {
        return _apiFailed(reason, needLogin, false);
    }


    @NotNull
    public BaseView _apiFailed(@NotNull String reason) {
        return _apiFailed(reason, false);
    }

    @NotNull
    public BaseView _needLogin(@NotNull String message) {
        return _apiFailed(message, true);
    }

    @NotNull
    public BaseView _needLogin() {
        return _needLogin("请当前未登录, 请登录后重试");
    }

    @NotNull
    public BaseView _needSuperAdmin(@NotNull String message) {
        return _apiFailed(message, false, true);
    }

    @NotNull
    public BaseView _needSuperAdmin() {
        return _needLogin("该功能只有超级管理员才能使用");
    }

    @NotNull
    public BaseView _needAdmin(@NotNull String message) {
        return _apiFailed(message, false, false, false, true);
    }

    @NotNull
    public BaseView _needAdmin() {
        return _needLogin("该功能只有管理员才能使用");
    }

    @NotNull
    public BaseView _needPassword(@NotNull String message) {
        return _apiFailed(message, false, false, true);
    }

    @NotNull
    public BaseView _needPassword() {
        return _needLogin("该功能需要密码, 请输入密码");
    }

    @NotNull
    public BaseView _needPermission(@NotNull String message) {
        return _apiFailed(message,
                false,
                false,
                false,
                false,
                true
        );
    }

    @NotNull
    public BaseView _needPermission() {
        return _needLogin("您无权使用该功能");
    }

    @NotNull
    public BaseView _needUnlock(@NotNull String message) {
        return _apiFailed(message, false, true);
    }

    @NotNull
    public BaseView _needUnlock() {
        return _needLogin("您当前已锁屏, 请先解锁");
    }


    @NotNull
    public BaseView _needDeveloper(@NotNull String message) {
        return _apiFailed(message,
                false,
                false,
                false,
                false,
                false,
                false,
                true
        );
    }

    @NotNull
    public BaseView _needDeveloper() {
        return _needLogin("您无权使用该功能");
    }
}
