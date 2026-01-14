package sequential_problems;

import com.hstairs.ppmajal.extraUtils.PlannerUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class IPCDatasetInterferenceFreeTest {

    private static class RunResult {
        final boolean isIF;
        final int totalConditions;
        final int ifConditions;

        RunResult(boolean isIF, int totalConditions, int ifConditions) {
            this.isIF = isIF;
            this.totalConditions = totalConditions;
            this.ifConditions = ifConditions;
        }
    }

    @Test
    public void testIPCDomainsInterferenceFree() throws Exception {
        final Path root = Paths.get("ipc2023-dataset-main");
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            System.out.println("[IPC2023-IF][WARN] Directory non trovata: " + root.toAbsolutePath());
            return;
        }

        final AtomicInteger totalPairs = new AtomicInteger(0);
        final AtomicInteger okPairs = new AtomicInteger(0);
        final AtomicInteger errorPairs = new AtomicInteger(0);
        final AtomicInteger skippedPairs = new AtomicInteger(0);
        final List<ResultRow> table = new ArrayList<>();

        try (Stream<Path> domains = Files.list(root)) {
            domains
                    .filter(Files::isDirectory)
                    .filter(dir -> {
                        String name = dir.getFileName().toString().toLowerCase();
                        return name.equals("farmland") ||
                                name.equals("block-grouping") ||
                                name.equals("drone") ||
                                name.equals("expedition") ||
                                name.equals("ext-plant-watering") ||
                                name.equals("hydropower") ||
                                name.equals("martkettrader") ||
                                name.equals("rover") ||
                                name.equals("sailing") ||
                                name.equals("sugar") ||
                                name.equals("counters") ;
                    })
                    .forEach(domainDir -> {
                        Path domainFile = domainDir.resolve("domain.pddl");
                        Path instancesDir = domainDir.resolve("instances");
                        if (!Files.exists(domainFile) || !Files.isRegularFile(domainFile)) return;
                        if (!Files.exists(instancesDir) || !Files.isDirectory(instancesDir)) return;

                        final String domainPath = normalize(domainFile);

                        try (Stream<Path> probs = Files.walk(instancesDir)) {
                            probs.filter(Files::isRegularFile)
                                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pddl"))
                                    .forEach(prob -> {
                                        totalPairs.incrementAndGet();
                                        final String problemPath = normalize(prob);
                                        final String domainName = domainDir.getFileName().toString();
                                        final String problemName = root.relativize(prob).toString().replace('\\','/');

                                        try {
                                            ExecutorService exec = Executors.newSingleThreadExecutor();

                                            Future<RunResult> fut = exec.submit(() -> {
                                                PlannerUtils pu = new PlannerUtils();
                                                pu.setIdf(true);
                                                boolean isIF = pu.isInterferenceFree(domainPath, problemPath);

                                                int total = 0;
                                                int ifCount = 0;
                                                try {
                                                    Field hField = PlannerUtils.class.getDeclaredField("h");
                                                    hField.setAccessible(true);
                                                    Object hObj = hField.get(pu);

                                                    if (hObj != null && hObj.getClass().getSimpleName().equals("H1")) {
                                                        Method mIF = hObj.getClass().getMethod("getInterferenceFreeConditionsCount");
                                                        ifCount = (int) mIF.invoke(hObj);

                                                        Method mTotal = hObj.getClass().getMethod("getAllComparisons");
                                                        Collection<?> c = (Collection<?>) mTotal.invoke(hObj);
                                                        total = c.size();
                                                    }
                                                } catch (Exception e) {
                                                }

                                                return new RunResult(isIF, total, ifCount);
                                            });

                                            try {
                                                RunResult res = fut.get(30, TimeUnit.SECONDS);
                                                okPairs.incrementAndGet();
                                                System.out.println("[IPC2023-IF] domain=" + domainName
                                                        + " problem=" + problemName
                                                        + " isIF=" + res.isIF
                                                        + " Total=" + res.totalConditions
                                                        + " IF_Count=" + res.ifConditions);

                                                table.add(new ResultRow(domainName, problemName, "OK",
                                                        String.valueOf(res.isIF),
                                                        String.valueOf(res.totalConditions),
                                                        String.valueOf(res.ifConditions)));

                                            } catch (TimeoutException te) {
                                                skippedPairs.incrementAndGet();
                                                fut.cancel(true);
                                                System.out.println("[IPC2023-IF][SKIPPED] domain=" + domainName
                                                        + " problem=" + problemName
                                                        + " reason=timeout(30s)");
                                                table.add(new ResultRow(domainName, problemName, "SKIPPED", "", "", ""));
                                            } catch (ExecutionException ex) {
                                                errorPairs.incrementAndGet();
                                                Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                                                System.out.println("[IPC2023-IF][ERROR] domain=" + domainName
                                                        + " problem=" + problemName
                                                        + " msg=" + cause.getClass().getSimpleName() + ": " + safeMsg(cause));
                                                table.add(new ResultRow(domainName, problemName, "ERROR", "", "", ""));
                                            } catch (InterruptedException ex) {
                                                skippedPairs.incrementAndGet();
                                                System.out.println("[IPC2023-IF][SKIPPED] domain=" + domainName
                                                        + " problem=" + problemName
                                                        + " reason=interrupted");
                                                Thread.currentThread().interrupt();
                                                table.add(new ResultRow(domainName, problemName, "SKIPPED", "", "", ""));
                                            } finally {
                                                exec.shutdownNow();
                                            }
                                        } catch (Throwable ex) {
                                            errorPairs.incrementAndGet();
                                            System.out.println("[IPC2023-IF][ERROR] domain=" + domainName
                                                    + " problem=" + problemName
                                                    + " msg=" + ex.getClass().getSimpleName() + ": " + safeMsg(ex));
                                            table.add(new ResultRow(domainName, problemName, "ERROR", "", "", ""));
                                        }
                                    });
                        } catch (IOException e) {
                            System.out.println("[IPC2023-IF][ERROR] Impossibile esplorare instances per " + domainDir + ": " + e.getMessage());
                        }
                    });
        }

        System.out.println("[IPC2023-IF][SUMMARY] pairsTotal=" + totalPairs.get()
                + " ok=" + okPairs.get()
                + " errors=" + errorPairs.get()
                + " skipped=" + skippedPairs.get());

        printResultsTable(table);
    }

    private static String normalize(Path p) {
        return p.toString().replace('\\', '/');
    }

    private static String safeMsg(Throwable t) {
        String m = t.getMessage();
        if (m == null || m.isEmpty()) return "(no message)";
        if (m.length() > 300) return m.substring(0, 300) + "...";
        return m;
    }

    // Aggiunti campi per le nuove colonne
    private static class ResultRow {
        final String domain;
        final String problem;
        final String status;
        final String isIF;
        final String totalCond;
        final String ifCond;

        ResultRow(String domain, String problem, String status, String isIF, String totalCond, String ifCond) {
            this.domain = domain;
            this.problem = problem;
            this.status = status;
            this.isIF = isIF == null ? "" : isIF;
            this.totalCond = totalCond == null ? "" : totalCond;
            this.ifCond = ifCond == null ? "" : ifCond;
        }
    }

    private static void printResultsTable(List<ResultRow> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("[IPC2023-IF][TABLE] Nessuna coppia dominio-problema analizzata.");
            return;
        }

        final String hDomain = "Domain";
        final String hProblem = "Problem";
        final String hStatus = "Status";
        final String hIsIF = "isIF";
        final String hTotal = "Total Conds";
        final String hIfC = "IF Conds";

        int wDomain = hDomain.length();
        int wProblem = hProblem.length();
        int wStatus = hStatus.length();
        int wIsIF = hIsIF.length();
        int wTotal = hTotal.length();
        int wIfC = hIfC.length();

        for (ResultRow r : rows) {
            wDomain = Math.max(wDomain, safe(r.domain).length());
            wProblem = Math.max(wProblem, safe(r.problem).length());
            wStatus = Math.max(wStatus, safe(r.status).length());
            wIsIF = Math.max(wIsIF, safe(r.isIF).length());
            wTotal = Math.max(wTotal, safe(r.totalCond).length());
            wIfC = Math.max(wIfC, safe(r.ifCond).length());
        }

        final String border = "+" + repeat('-', wDomain + 2) + "+"
                + repeat('-', wProblem + 2) + "+"
                + repeat('-', wStatus + 2) + "+"
                + repeat('-', wIsIF + 2) + "+"
                + repeat('-', wTotal + 2) + "+"
                + repeat('-', wIfC + 2) + "+";

        System.out.println("[IPC2023-IF][TABLE] Riepilogo risultati:");
        System.out.println(border);
        System.out.println("| " + pad(hDomain, wDomain) + " | "
                + pad(hProblem, wProblem) + " | "
                + pad(hStatus, wStatus) + " | "
                + pad(hIsIF, wIsIF) + " | "
                + pad(hTotal, wTotal) + " | "
                + pad(hIfC, wIfC) + " |");
        System.out.println(border);
        for (ResultRow r : rows) {
            System.out.println("| " + pad(safe(r.domain), wDomain) + " | "
                    + pad(safe(r.problem), wProblem) + " | "
                    + pad(safe(r.status), wStatus) + " | "
                    + pad(safe(r.isIF), wIsIF) + " | "
                    + pad(safe(r.totalCond), wTotal) + " | "
                    + pad(safe(r.ifCond), wIfC) + " |");
        }
        System.out.println(border);
    }

    private static String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private static String repeat(char ch, int count) {
        if (count <= 0) return "";
        char[] arr = new char[count];
        Arrays.fill(arr, ch);
        return new String(arr);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}