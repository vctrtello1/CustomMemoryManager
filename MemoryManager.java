import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class MemoryObject {
    String objectId;
    AtomicInteger referenceCount;

    MemoryObject(String objectId) {
        this.objectId = objectId;
        this.referenceCount = new AtomicInteger(0);
    }

    @Override
    public String toString() {
        return "MemoryObject{id='" + objectId + "', referenceCount=" + referenceCount.get() + "}";
    }
}

class MemoryManager {
    private ConcurrentHashMap<String, MemoryObject> memoryPool = new ConcurrentHashMap<>();

    public void createObject(String objectId) {
        memoryPool.putIfAbsent(objectId, new MemoryObject(objectId));
    }

    public void addReference(String objectId) {
        memoryPool.computeIfAbsent(objectId, MemoryObject::new)
                  .referenceCount.incrementAndGet();
    }

    public void removeReference(String objectId) {
        memoryPool.computeIfAbsent(objectId, MemoryObject::new)
                  .referenceCount.decrementAndGet();
    }

    public synchronized void garbageCollect() {
        memoryPool.entrySet().removeIf(e -> e.getValue().referenceCount.get() <= 0);
    }

    public List<MemoryObject> getMemoryPool() {
        if (memoryPool.size() > 3) {
            garbageCollect();
        }
        return memoryPool.values().stream()
                .filter(obj -> obj.referenceCount.get() >= 0)
                .sorted(Comparator.comparing(obj -> obj.objectId))
                .collect(Collectors.toList());
    }
}

public class Solution {
    public static void executeCommand(String command, MemoryManager memoryManager) {
        String[] commandParts = command.split(" ", 3);
        String action = commandParts[0];
        if (action.equals("addReference")) {
            String object = commandParts[1];
            memoryManager.addReference(object);
        } else if (action.equals("removeReference")) {
            String object = commandParts[1];
            memoryManager.removeReference(object);
        } else if (action.equals("garbageCollect")) {
            memoryManager.garbageCollect();
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int commandsCount = Integer.parseInt(bufferedReader.readLine().trim());

        List<String> commands = new ArrayList<>();

        for (int i = 0; i < commandsCount; i++) {
            String commandsItem = bufferedReader.readLine();
            commands.add(commandsItem);
        }

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        List<Future<Void>> futures = new ArrayList<>();

        MemoryManager memoryManager = new MemoryManager();
        for (String command : commands) {
            Future<Void> future = threadPoolExecutor.submit(() -> {
                executeCommand(command, memoryManager);
                return null;
            });
            futures.add(future);
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        threadPoolExecutor.shutdown();

        // Collect the memory pool state after all commands have been processed
        List<String> result = new ArrayList<>();
        for (MemoryObject memoryObject : memoryManager.getMemoryPool()) {
            result.add(memoryObject.toString());

        }

        for (int i = 0; i < result.size(); i++) {
            bufferedWriter.write(result.get(i));

            if (i != result.size() - 1) {
                bufferedWriter.write("\n");
            }
        }
        bufferedWriter.newLine();

        bufferedReader.close();
        bufferedWriter.close();
    }
}
