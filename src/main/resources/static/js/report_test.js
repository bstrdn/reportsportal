function demo() {
    alert("TEST");
}

var table = $('#datatable').dataTable( {
    stateSave: true,
    "iDisplayLength": 20,
    "aLengthMenu": [[ 10, 20, 50, 100 ,-1],[10,20,50,100,"Все"]],
    language: {
        "processing": "Подождите...",
        "search": "Поиск:",
        "lengthMenu": "Показать _MENU_ записей",
        "info": "Записи с _START_ до _END_ из _TOTAL_ записей",
        "infoEmpty": "Записи с 0 до 0 из 0 записей",
        "infoFiltered": "(отфильтровано из _MAX_ записей)",
        "infoPostFix": "",
        "loadingRecords": "Загрузка записей...",
        "zeroRecords": "Записи отсутствуют.",
        "emptyTable": "В таблице отсутствуют данные",
        "paginate": {
            "first": "Первая",
            "previous": "Предыдущая",
            "next": "Следующая",
            "last": "Последняя"
        },
        "aria": {
            "sortAscending": ": активировать для сортировки столбца по возрастанию",
            "sortDescending": ": активировать для сортировки столбца по убыванию"
        }
    },
    "dom": '<"dt-buttons"Bf><"clear">lirtp',
    dom: 'Bfrtip',
    buttons: [
        'copyHtml5',
        'excelHtml5',
        'csvHtml5',
        'pdfHtml5'
    ],
    "paging": true,
    "autoWidth": true,
    "ajax": {
        // "type" : "GET",
        "url" : "rest/report1",
        "dataSrc": function ( json ) {
            return json;
        }
    },
    "columns": [
        { "data": "fullname" },
        { "data": "fullname" },
        { "data": "fullname" }
    ]
} );

var tableTools = new $.fn.dataTable.TableTools(table);
$(tableTools.fnContainer()).insertBefore('datatable_wrapper');

