package ru.bstrdn.report.gate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.bstrdn.report.gate.repository.GateRepository;
import ru.bstrdn.report.postgres.repository.GateEventsRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Service
public class GateUtil {
    @Autowired
    GateRepository gateRepository;
    @Autowired
    GateEventsRepository gateEventsRepository;

    public String copyDir(String sourceDirName, String targetSourceDir) throws IOException {
        File folder = new File(sourceDirName);
        int count = 0;
        StringBuilder sb = new StringBuilder();

        File[] listOfFiles = folder.listFiles();

        Path destDir = Paths.get(targetSourceDir);
        if (listOfFiles != null)
            for (File file : listOfFiles)
                if (Pattern.matches("(.*).mdb", file.getName())) {
                    try {
                        Files.copy(file.toPath(), destDir.resolve(file.getName()));
                        gateEventsRepository.saveAll(gateRepository.getGateEvents(file.getCanonicalPath()));
                        sb.append(" //" + file.getName());
                        count++;
                    } catch (FileAlreadyExistsException e) {

                    }
                }
        sb.insert(0, "GATE: EVENTS: Всего файлов добавлено: " + count + ":");
        return sb.toString();
    }
}
