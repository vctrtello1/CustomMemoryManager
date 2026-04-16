import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class MemoryObject {
    String id;
    int referenceCount;

    MemoryObject(String id) {
        this.id = id;
        this.referenceCount = 0;
    }

    @Override
    public String toString() {
        return "MemoryObject{" +
                "id='" + id + '\'' +
                ", referenceCount=" + referenceCount +
                '}';
    }
}

class MemoryManager {
    // Implement the MemoryManager class here

    public void createObject(String objectId) {
        // Write your code here
    }

    public void addReference(String objectId) {
        // Write your code here
    }

    public void removeReference(String objectId) {
        // Write your code here
    }

    public void garbageCollect() {
        // Write your code here
    }

    public List<MemoryObject> getMemoryPool() {
        // Write your code here
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
