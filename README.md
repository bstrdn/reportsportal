**Gate**
Папка для хранения .mdb файлов от гейт: C:/GATE/BackupEvents
На сервера скуда нужно настроить общий доступ к папке Server: \\skud\Server
Ежедневные задания:
    Копирование таблицы Users из файла config.mdb "0 0 7 * * ?" (каждый день в 7 утра)
    Копирование таблиц Events из папки \\skud\Server\Events "0 0 6 * * ?" (каждый день в 6 утра)
