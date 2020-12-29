-- 1 ОТЧЕТ
SELECT cl.fullname, --"ФИО Пациента"
       s.fixdate, --"День обращения"
       s.workdate, -- "Записан на дату"
       doc.fullname docFullname --"Записан к доктору"
       --s.clvisit  --Проверка статуса отметки о посещении
FROM schedule s
         INNER JOIN clients cl ON s.pcode = cl.pcode
         LEFT JOIN doctor doc ON s.dcode = doc.dcode
WHERE s.status = 1                          --Статус назначения "Первичный"
  AND s.fixdate BETWEEN '2020-01-26 08:43:48' AND '2020-02-26 08:43:48'
  --AND s.clvisit = 1 --Отрабатывает с галкой "Пришли на прием"
  AND (s.clvisit IS NULL OR s.clvisit != 1) --Отрабатывает с галкой "НЕ пришли на прием"
ORDER BY s.workdate DESC--cl.fullname;