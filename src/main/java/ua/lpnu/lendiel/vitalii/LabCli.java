package ua.lpnu.lendiel.vitalii;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Shared file options for the command-line laboratory applications. */
public record LabCli(Path input, Path output, List<String> positional,
        boolean help, boolean version) {
    public LabCli {
        positional = List.copyOf(positional);
    }

    public static LabCli parse(String[] args) {
        Objects.requireNonNull(args, "Arguments cannot be null");
        Path input = null;
        Path output = null;
        List<String> positional = new ArrayList<>();
        boolean help = false;
        boolean version = false;
        for (int index = 0; index < args.length; index++) {
            String arg = args[index];
            if (arg == null) {
                throw new IllegalArgumentException("null argument");
            }
            switch (arg) {
                case "--help", "-h" -> help = true;
                case "--version" -> version = true;
                case "--input", "-i" -> input = path(value(args, ++index, arg), arg);
                case "--output", "-o" -> output = path(value(args, ++index, arg), arg);
                default -> {
                    if (arg.startsWith("--input=")) {
                        input = path(arg.substring("--input=".length()), "--input");
                    } else if (arg.startsWith("--output=")) {
                        output = path(arg.substring("--output=".length()), "--output");
                    } else if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("unknown option: " + arg);
                    } else {
                        positional.add(arg);
                    }
                }
            }
        }
        return new LabCli(input, output, positional, help, version);
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length || args[index] == null
                || args[index].isBlank() || args[index].startsWith("--")) {
            throw new IllegalArgumentException(option + " requires a path");
        }
        return args[index];
    }

    private static Path path(String value, String option) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(option + " requires a path");
        }
        return Path.of(value);
    }

    /** Reads an explicit UTF-8 file, or the bundled fixture when input is absent. */
    public String[] readLines(Class<?> anchor, String resource) throws IOException {
        String text;
        if (input != null) {
            text = Files.readString(input, StandardCharsets.UTF_8);
        } else {
            try (var stream = anchor.getResourceAsStream(resource)) {
                if (stream == null) {
                    throw new NoSuchFileException(resource);
                }
                text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return text.lines().toArray(String[]::new);
    }

    /** Prints a report and optionally saves the identical UTF-8 text. */
    public void writeReport(String report) throws IOException {
        if (output != null) {
            Path parent = output.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);
        }
        System.out.print(report);
    }
}
