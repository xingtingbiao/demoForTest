package com.xingtb.LDAP;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * ldap
 *
 * @author vito
 */
public class LdapService {
    /*
     * DC  (Domain Component)
     * CN  (Common Name)
     * OU  (Organizational Unit)
     */

    private Control[] connCtls = null;

    private static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    
    private static final String AUTHENTICATION = "simple";

    private static final String REFERRAL = "ignore";

    private static final String LDAP_SYSTEM_NAME = "njadmin";

    private static final String LDAP_SYSTEM_PASSWORD = "Welcome123";

    private static final String LDAP_URL = "ldap://znlhzl.imwork.net:3899/";

    private static final String SEARCH_BASE_DN = "dc=znlhzl,dc=org";


    private ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    private String lastTimeMatchWord = "";

    private LdapService() {
    }

    private static class LdapAuthenticationProviderHelper {
        public static LdapService helper = new LdapService();
    }

    public static LdapService getInstance() {
        return LdapAuthenticationProviderHelper.helper;
    }

    public boolean connect(final String username, final String inputPassword) {
        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                LdapContext ctx = null;
                boolean isValid;
                try {
                    threadLocal.set(lastTimeMatchWord);
                    long start = System.currentTimeMillis();
                    ctx = connectLdap();
                    long connectLdapTime = System.currentTimeMillis() - start;
                    System.out.println("admin connect: " + connectLdapTime + "ms.");
                    isValid = authenticate(ctx, username, inputPassword);
                    System.out.println("login connect: " + (System.currentTimeMillis() - connectLdapTime - start) + "ms.");
                } catch (Exception e) {
                    return false;
                } finally {
                    closeContext(ctx);
                    threadLocal.remove();
                }
                return isValid;
            }
        });
        futureTask.run();
        try {
            return futureTask.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    private LdapContext connectLdap() {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
        String url = LDAP_URL;
        if (!isBlank(url)) {
            if (!url.endsWith("/")) {
                url += "/";
            }
        }
        props.put(Context.PROVIDER_URL, url);
        props.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION);
        props.put(Context.REFERRAL, REFERRAL);
        if (!isBlank(LDAP_SYSTEM_NAME) && !isBlank(LDAP_SYSTEM_PASSWORD)) {
            props.put(Context.SECURITY_PRINCIPAL, LDAP_SYSTEM_NAME);
            props.put(Context.SECURITY_CREDENTIALS, LDAP_SYSTEM_PASSWORD);
        }
        try {
            return new InitialLdapContext(props, connCtls);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            System.out.println("AuthenticationException，Authentication faild: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception,Something wrong while authenticating: " + e.toString());
        }
        return null;
    }

    private String recurseGetUserDN(final LdapContext ctx, final String name) {
        String lastTimeMatchWordShadow = threadLocal.get();
        String res = "";
        if (!isEmpty(lastTimeMatchWordShadow)) {
            res = getUserDN(ctx, name, lastTimeMatchWordShadow);
            if (!isBlank(res)) {
                return res;
            }
        }
        String[] fWords = {"sAMAccountName", "cn", "userPrincipalName", "uid", "displayName", "name", "sn",};
        for (String fWord : fWords) {
            if (!isEmpty(lastTimeMatchWordShadow) && lastTimeMatchWordShadow.equals(fWord)) {
                continue;
            }
            res = getUserDN(ctx, name, fWord);
            if (!isBlank(res)) {
                lastTimeMatchWord = fWord;
                break;
            }
        }
        return res;
    }

    private String getUserDN(LdapContext ctx, String name, String word) {
        String userDN = "";
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "(&(" + word + "=" + name + "))";
            NamingEnumeration en = ctx.search(SEARCH_BASE_DN, filter, constraints);
            while (en != null && en.hasMoreElements()) {
                if (en.hasMoreElements()) {
                    SearchResult sr = (SearchResult) en.nextElement();
                    userDN = sr.getNameInNamespace();
                    if (!isBlank(userDN) && userDN.contains(name)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userDN;
    }

    private boolean authenticate(LdapContext ctx, String ID, String password) {
        boolean isValid = true;
        String userDN = recurseGetUserDN(ctx, ID);
        if (isEmpty(userDN)) {
            userDN = ID;
        }
        try {
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDN);
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            ctx.reconnect(connCtls);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            isValid = false;
        } catch (NamingException e) {
            e.printStackTrace();
            isValid = false;
        }
        return isValid;
    }

    private void closeContext(LdapContext ctx) {
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBlank(String str) {

        int strLen;
        return (str == null || (strLen = str.length()) == 0) || isBlank(str, strLen);
    }

    private static boolean isBlank(String str, int strLen) {
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static void main(String[] args) {
        System.out.println("first:" + LdapService.getInstance().connect("xingtingbiao", "@xingtb321"));
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getName() + ":" + LdapService.getInstance().connect("cent1", "123456"));
//            }
//        };
//        for (int i = 0; i < 100; i++) {
//            new Thread(runnable).start();
//        }

    }
}