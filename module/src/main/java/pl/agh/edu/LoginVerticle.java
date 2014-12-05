package pl.agh.edu;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class LoginVerticle extends BusModBase {
    private Handler<Message<JsonObject>> loginHandler;
    private Handler<Message<JsonObject>> logoutHandler;
    private Handler<Message<JsonObject>> authoriseHandler;

    protected final Map<String, LoginInfo> sessions = new HashMap<>();

    private static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000;
    private long sessionTimeout = DEFAULT_SESSION_TIMEOUT;

    private static final class LoginInfo {
        final long timerID;
        final String login;
        final String group;

        private LoginInfo(String login, String group, long timerID) {
            this.timerID = timerID;
            this.login = login;
            this.group = group;
        }
    }


    @Override
    public void start() {
        super.start();
        loginHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                doLogin(message);
            }
        };
        eb.registerHandler("connect", loginHandler);
        logoutHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                doLogout(message);
            }
        };
        eb.registerHandler("disconnect", logoutHandler);
        authoriseHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                doAuthorise(message);
            }
        };
        eb.registerHandler("authorise", authoriseHandler);
    }

    private void doLogin(Message<JsonObject> message) {
        final String login = getMandatoryString("login", message);
        if(login == null) {
            return;
        }

        String group = getMandatoryString("group", message);
        if(group == null) {
            return;
        }

        container.logger().info("connected " + login);

        // Found
        final String sessionID = UUID.randomUUID().toString();
        long timerID = vertx.setTimer(sessionTimeout, new Handler<Long>() {
            public void handle(Long timerID) {
                container.logger().info("logging out: " + login);
                sessions.remove(sessionID);
                sendClientDisconnectedInfo(login);
            }
        });
        sessions.put(sessionID, new LoginInfo(login, group, timerID));
        
        sendClientConnectedInfo(login, group);
        
        JsonObject jsonReply = new JsonObject().putString("sessionID", sessionID)
                .putString("login", login)
                .putString("group", group);
        sendOK(message, jsonReply);
    }

    protected void doLogout(final Message<JsonObject> message) {
        final String sessionID = getMandatoryString("sessionID", message);
        if (sessionID != null) {
            if (logout(sessionID)) {
                sendOK(message);
            } else {
                super.sendError(message, "Not logged in");
            }
        }
    }

    protected boolean logout(String sessionID) {
        LoginInfo info = sessions.remove(sessionID);
        if (info != null) {
            container.logger().info("logging out: " + info.login);
            vertx.cancelTimer(info.timerID);
            
            sendClientDisconnectedInfo(info.login);
            
            return true;
        } else {
            return false;
        }
    }

    protected void doAuthorise(Message<JsonObject> message) {
        container.logger().info("authorising: " + message.body());
        String sessionID = getMandatoryString("sessionID", message);
        if (sessionID == null) {
            return;
        }
        LoginInfo info = sessions.get(sessionID);

        if (info != null) {
            JsonObject reply = new JsonObject().putString("login", info.login);
            container.logger().info("authorized: " + message.body());
            sendOK(message, reply);
        } else {
            container.logger().info("unauthorized: " + message.body());
            sendStatus("denied", message);
        }
    }

    private void sendClientConnectedInfo(String login, String group) {
    	JsonObject connectedInfo = new JsonObject().putString("login", login)
        		.putString("group", group);
        eb.publish("client.connected", connectedInfo);
    }
    
    private void sendClientDisconnectedInfo(String login) {
    	JsonObject disconnectedInfo = new JsonObject().putString("login", login);
        eb.publish("client.disconnected", disconnectedInfo);
    }
}