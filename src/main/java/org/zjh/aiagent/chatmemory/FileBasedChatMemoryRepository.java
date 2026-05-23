package org.zjh.aiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.jetbrains.annotations.NotNull;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * File-based ChatMemoryRepository using Kryo serialization.
 * Each conversation is stored as a separate binary file under the configured directory.
 */
public class FileBasedChatMemoryRepository implements ChatMemoryRepository {

    private static final String FILE_SUFFIX = ".kryo";

    private final Path storageDir;
    // per-conversation read-write locks to avoid file corruption under concurrency
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        // register common Spring AI message types for efficiency
        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
        kryo.register(UserMessage.class);
        kryo.register(AssistantMessage.class);
        kryo.register(SystemMessage.class);
        kryo.register(MessageType.class);
        return kryo;
    });

    public FileBasedChatMemoryRepository(String storageDir) {
        this.storageDir = Paths.get(storageDir);
        try {
            Files.createDirectories(this.storageDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create chat memory storage directory: " + storageDir, e);
        }
    }

    @NotNull
    @Override
    public List<String> findConversationIds() {
        try (Stream<Path> files = Files.list(storageDir)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(FILE_SUFFIX))
                    .map(p -> {
                        String name = p.getFileName().toString();
                        return name.substring(0, name.length() - FILE_SUFFIX.length());
                    })
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list conversation IDs", e);
        }
    }

    @NotNull
    @Override
    public List<Message> findByConversationId(@NotNull String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Path file = resolveFile(conversationId);
        if (!Files.exists(file)) {
            return List.of();
        }
        ReentrantReadWriteLock.ReadLock readLock = getLock(conversationId).readLock();
        readLock.lock();
        try (Input input = new Input(new FileInputStream(file.toFile()))) {
            @SuppressWarnings("unchecked")
            List<Message> messages = kryoThreadLocal.get().readObject(input, ArrayList.class);
            return new ArrayList<>(messages);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read conversation: " + conversationId, e);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void saveAll(@NotNull String conversationId, @NotNull List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        Path file = resolveFile(conversationId);
        ReentrantReadWriteLock.WriteLock writeLock = getLock(conversationId).writeLock();
        writeLock.lock();
        try (Output output = new Output(new FileOutputStream(file.toFile()))) {
            kryoThreadLocal.get().writeObject(output, new ArrayList<>(messages));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save conversation: " + conversationId, e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deleteByConversationId(@NotNull String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Path file = resolveFile(conversationId);
        ReentrantReadWriteLock.WriteLock writeLock = getLock(conversationId).writeLock();
        writeLock.lock();
        try {
            Files.deleteIfExists(file);
            locks.remove(conversationId);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete conversation: " + conversationId, e);
        } finally {
            writeLock.unlock();
        }
    }

    private Path resolveFile(String conversationId) {
        return storageDir.resolve(conversationId + FILE_SUFFIX);
    }

    private ReentrantReadWriteLock getLock(String conversationId) {
        return locks.computeIfAbsent(conversationId, id -> new ReentrantReadWriteLock());
    }
}
