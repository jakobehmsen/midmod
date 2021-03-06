package jorch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static DefaultListModel<Runnable> tasksModel;

    private static void requestHalt(Runnable activator) {
        /*SwingUtilities.invokeLater(() -> {
            tasksModel.addElement(activator);
        });*/
        procedureList.requestHalt(activator);
    }

    private static <T> T requestHaltCall(Callable<T> activator) {
        CountDownLatch latch = new CountDownLatch(1);
        T[] resultHolder = (T[])new Object[1];

        Future<T> future = executorService.submit(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return resultHolder[0];
        });

        requestHalt(new Runnable() {
            @Override
            public void run() {
                try {
                    resultHolder[0] = activator.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }

            @Override
            public String toString() {
                return activator.toString();
            }
        });

        /*SwingUtilities.invokeLater(() -> {
            tasksModel.addElement(new Runnable() {
                @Override
                public void run() {
                    try {
                        resultHolder[0] = activator.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }

                @Override
                public String toString() {
                    return activator.toString();
                }
            });
        });*/

        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ProcedureList procedureList;

    public static void main(String[] args) throws Exception {
        TaskFactory taskFactory = new ReflectiveTaskFactory(token -> new Scheduler(token, executorService),
            new TestTaskFactory(a -> procedureList.requestHalt(a), c -> requestHaltCall(c)));

        //SQLRepository repository = new SQLRepository(new MigratingSerializer(), taskFactory);
        MySQLTokenRepository repository = new MySQLTokenRepository(new MigratingSerializer(), taskFactory);

        if(!repository.exists()) {
            repository.create();
        }

        ArrayList<Supplier<TaskSelector>> procedures = new ArrayList<>();

        procedures.add(new Supplier<TaskSelector>() {
            @Override
            public TaskSelector get() {
                return new TaskSelector("forkAndMerge", new Object[]{});
            }

            @Override
            public String toString() {
                return "Procedure fork-and-merge";
            }
        });

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);

        cfg.setDirectoryForTemplateLoading(new File("./templates"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);

        procedureList = new ProcedureList(
            taskSelector -> {
                try {
                    RepositoryBasedToken token = repository.newToken(taskSelector);
                    executorService.execute(() -> {
                        token.proceed();
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, Arrays.asList(
            new Procedure("Procedure fork-and-merge", () -> new TaskSelector("forkAndMerge", new Object[]{}))/*,
        new Procedure("Procedure 2"),
        new Procedure("Procedure 3"),
        new Procedure("Procedure 4")*/
        ));

        Resolver resolver = new Resolver(procedureList);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) throws IOException {
                String[] uriParts = t.getRequestURI().getPath().toString().split("/");
                String target = uriParts[2];
                String action = uriParts.length > 3 ? uriParts[3] : null;
                java.util.Scanner s = new java.util.Scanner(t.getRequestBody()).useDelimiter("\\A");
                String requestBody = s.hasNext() ? s.next() : t.getRequestURI().getQuery();
                requestBody = requestBody == null ? "" : requestBody;
                String encoding = "ISO-8859-1";
                List<String> args = Arrays.asList(requestBody.split("&")).stream().filter(x -> x.length() > 0).map(x -> {
                    String[] parts = x.split("=");
                    try {
                        for(int i = 0; i < parts.length; i++)
                            parts[i] = URLDecoder.decode(parts[i], encoding);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return parts.length == 2 ? parts[1] : null;
                }).collect(Collectors.toList());
                Object webCompatible = resolver.resolve(target, action, args);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                int status = 200;

                try {
                    Template temp = cfg.getTemplate(target + ".ftlh");
                    temp.process(webCompatible, new OutputStreamWriter(byteArrayOutputStream));

                    if(action != null) {
                        status = 301;
                        t.getResponseHeaders().add("Location", Arrays.asList(uriParts).stream().limit(3).collect(Collectors.joining("/")));
                    }
                } catch (TemplateException e) {
                    e.printStackTrace();
                }

                t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate, private");

                byte[] bytes = byteArrayOutputStream.toByteArray();
                t.sendResponseHeaders(status, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            }
        });
        server.setExecutor(null); // creates a default executor
        server.start();

        if(1 != 2)
            return;

        DefaultListModel<Supplier<TaskSelector>> proceduresModel = new DefaultListModel<>();
        JList<Supplier<TaskSelector>> proceduresView = new JList<>(proceduresModel);
        procedures.forEach(p -> proceduresModel.addElement(p));
        proceduresView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && proceduresView.getSelectedIndex() != -1) {
                    Supplier<TaskSelector> procedure = proceduresView.getSelectedValue();
                    TaskSelector taskSelector = procedure.get();

                    try {
                        RepositoryBasedToken token = repository.newToken(taskSelector);
                        executorService.execute(() -> {
                            token.proceed();
                        });
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        JFrame f = new JFrame();
        JPanel contentPane2 = (JPanel) f.getContentPane();

        JScrollPane proceduresViewScrollPane = new JScrollPane(proceduresView);
        proceduresViewScrollPane.setBorder(BorderFactory.createTitledBorder("Procedures"));
        contentPane2.add(proceduresViewScrollPane, BorderLayout.WEST);

        tasksModel = new DefaultListModel<>();
        JList<Runnable> tasksView = new JList<>(tasksModel);

        Consumer<Token> addSS = token -> {
            token.getEventChannel().add(new TokenListener() {
                @Override
                public void wasPassed() {
                    executorService.execute(() -> {
                        ((RepositoryBasedToken)token).proceed();
                    });
                }

                @Override
                public void finished() {

                }

                @Override
                public void wasClosed() {

                }
            });
        };

        BiFunction<Token, Boolean, TokenContainerListener> tokenListenerFunction = new BiFunction<Token, Boolean, TokenContainerListener>() {
            @Override
            public TokenContainerListener apply(Token tokenX, Boolean atLoad) {
                return new TokenContainerListener() {
                    private boolean loading = atLoad;

                    @Override
                    public void addedToken(Token token2) {
                        if(((RepositoryBasedToken)token2).hasMore() || token2.getParent() == null)
                            addSS.accept(token2);

                        token2.getEventChannel().add(this);

                        synchronized (this) {
                            if (loading && ((RepositoryBasedToken)token2).hasMore() && ((RepositoryBasedToken)token2).isWaiting()) {
                                executorService.execute(() -> {
                                    RepositoryBasedToken t = (RepositoryBasedToken) token2;
                                    t.proceed();
                                });

                                loading = false;
                            }
                        }
                    }
                };
            }
        };
        repository.allTokens().forEach(ss -> {
            tokenListenerFunction.apply(ss, true).addedToken(ss);
        });
        tasksView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && tasksView.getSelectedIndex() != -1) {
                    Runnable t = tasksView.getSelectedValue();
                    executorService.execute(() -> {
                        t.run();
                        tasksModel.removeElement(t);
                    });
                }
            }
        });

        repository.getEventChannel().add((TokenContainerListener) token -> {
            addSS.accept(token);

            token.getEventChannel().add(new TokenContainerListener() {
                @Override
                public void addedToken(Token token2) {
                    addSS.accept(token2);

                    token2.getEventChannel().add(this);
                }
            });
        });

        JScrollPane tasksViewScrollPane = new JScrollPane(tasksView);
        tasksViewScrollPane.setBorder(BorderFactory.createTitledBorder("Tasks"));
        contentPane2.add(tasksViewScrollPane, BorderLayout.CENTER);

        f.setSize(640, 480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
