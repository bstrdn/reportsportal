var date = new Date();
var day = date.getDate(),
    month = date.getMonth() + 1,
    year = date.getFullYear(),
    month = (month < 10 ? "0" : "") + month;
day = (day < 10 ? "0" : "") + day;
var today = year + "-" + month + "-01";
document.getElementById('endDate').value = today;
var lastMonth = (month == 1 ? year - 1 : year) + "-" + (month == 1 ? 12 : month - 1) + "-01";
document.getElementById('startDate').value = lastMonth;

//
// function demo() {
//     table
//         .clear()
//         .draw();
//     alert("test");
//
// }

function getRadio() {
    var allradio = document.getElementsByName('radiorep');
    for (var i = 0; i < allradio.length; i++) {
        if (allradio[i].checked) {
            return allradio[i].value;
        }
    }
}


//РАБОЧАЯ С ТОПДЖАВА
var ctx = {
    ajaxUrl: "rest/report1",
    updateTable: function () {
        $.ajax({
            type: "GET",
            url: "rest/report1",
            data: {
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                radio: getRadio(),
                department: $('#allDepartmentWithId').val(),
                registrar: $('#allRegistrarWithId').val()
            },
        }).done(updateTableByData);
    }
};


function makeEditable(datatableOpts) {
    ctx.datatableApi = $("#datatable").DataTable(
        // https://api.jquery.com/jquery.extend/#jQuery-extend-deep-target-object1-objectN
        $.extend(true, datatableOpts,
            {
                "ajax": {
                    "url": "rest/report1",
                    "dataSrc": ""
                },
                stateSave: true,
                retrieve: true,
                destroy: true,
                "iDisplayLength": 20,
                "aLengthMenu": [[10, 20, 50, 100, -1], [10, 20, 50, 100, "Все"]],
                "dom": '<"dt-buttons"Bfli>rtp',
                "paging": true,
                "autoWidth": true,
                "fixedHeader": true,
                "buttons": [
                    'pdf',
                    'excelHtml5',
                    'copyHtml5',
                    'colvis',
                    {
                        extend: 'print',
                        text: 'Печать',
                        autoPrint: true,
                        exportOptions: {
                            columns: ':visible',
                        },
                        customize: function (win) {
                            $(win.document.body).find('table').addClass('display').css('font-size', '9px');
                            $(win.document.body).find('tr:nth-child(odd) td').each(function (index) {
                                $(this).css('background-color', '#D0D0D0');
                            });
                            $(win.document.body).find('h1').css('text-align', 'center');
                        }
                    }
                ],
                language: {
                    "processing": "Подождите...",
                    "search": "Поиск:",
                    "lengthMenu": "Показать _MENU_ пациентов",
                    "info": "Всего _TOTAL_ пациентов",
                    // "info": "Пациенты с _START_ по _END_ из _TOTAL_ пациентов",
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
                }
            }
        ));
}


$(function () {
    makeEditable({
        "columns": [
            {"data": "fullname"},
            {"data": "fixdate"},
            {"data": "workdate"},
            {"data": "docFullname"},
            {"data": "phone1"}
        ],
        "order": [
            [
                0,
                "asc"
            ]
        ]
    });

});


function updateTableByData(data) {
    ctx.datatableApi.clear().rows.add(data).draw();
}


function clearFilter() {
    $("#filter")[0].reset();
    $.get(mealAjaxUrl, updateTableByData);
}

