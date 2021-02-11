package ru.bstrdn.report.gate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bstrdn.report.gate.repository.GateRepository;
import ru.bstrdn.report.postgres.repository.GateEventsRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class GateUtil {
    @Autowired
    GateRepository gateRepository;
    @Autowired
    GateEventsRepository gateEventsRepository;
    private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("YYMMdd");


    public String copyDir(String sourceDirName, String targetSourceDir) throws IOException {

        String todayMdb = "n" + DATETIME_FORMATTER.format(LocalDate.now()) + ".mdb";

        File folder = new File(sourceDirName);
        int count = 0;
        StringBuilder sb = new StringBuilder();

        File[] listOfFiles = folder.listFiles();

        Path destDir = Paths.get(targetSourceDir);
        if (listOfFiles != null)
            for (File file : listOfFiles) {
                String fileName = file.getName();
                if (Pattern.matches("(.*).mdb", fileName) && !fileName.equals(todayMdb)) {
                    try {
                        Files.copy(file.toPath(), destDir.resolve(fileName));
                        gateEventsRepository.saveAll(gateRepository.getGateEvents(file.getCanonicalPath()));
                        sb.append(" //" + fileName);
                        count++;
                    } catch (FileAlreadyExistsException e) {

                    }
                }
            }
        sb.insert(0, "GATE: EVENTS: Всего файлов добавлено: " + count + ":");
        return sb.toString();
    }
}
