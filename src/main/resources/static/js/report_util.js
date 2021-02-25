// var reportName = $('#reportName').val()
// var resturl = 'rest/' + reportName;

var date = new Date();
var day = date.getDate();
month = date.getMonth() + 1;
year = date.getFullYear();
year = (month == 1 ? year - 1 : year);
startmonth = (month == 1 ? 12 : month - 1);
lmonth = (startmonth < 10 ? "0" : "") + startmonth;
var enddate = year + "-" + lmonth + "-" + daysInMonth(lmonth, year);
document.getElementById('endDate').value = enddate;
var restPlug = 'rest/plug';

function getMonthWithO (month) {
    return (month < 10 ? "0" : "") + month;
}

function lastMonth() {

    document.getElementById('startDate').value = year + "-" + lmonth + "-" + "01";
    document.getElementById('endDate').value = enddate;


}

function currentMonth() {
    document.getElementById('startDate').value = year + "-" + getMonthWithO(month)  + "-" + "01";
    document.getElementById('endDate').value = year + "-" + getMonthWithO(month)  + "-" + day;

}



function daysInMonth(month, year) {
    return new Date(year, month, 0).getDate();
}

function getRadio() {
    var allradio = document.getElementsByName('radiorep');
    for (var i = 0; i < allradio.length; i++) {
        if (allradio[i].checked) {
            return allradio[i].value;
        }
    }
}

function getFilterCombine() {
    var allradio2 = document.getElementsByName('radio_combine');
    for (var i = 0; i < allradio2.length; i++) {
        if (allradio2[i].checked) {
            return allradio2[i].value;
        }
    }
}


//РАБОЧАЯ С ТОПДЖАВА!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// var ctx = {
//     ajaxUrl: resturl,
//     updateTable: function () {
//         $.ajax({
//             type: "GET",
//             url: resturl,
//             data: {
//                 reportName: reportName,
//                 startDate: $('#startDate').val(),
//                 endDate: $('#endDate').val(),
//                 radio: getRadio(),
//                 department: $('#allDepartmentWithId').val(),
//                 registrar: $('#allRegistrarWithId').val()
//             },
//         }).done(updateTableByData);
//     }
// };


function makeEditable(datatableOpts) {
    ctx.datatableApi = $("#datatable").DataTable(
        // https://api.jquery.com/jquery.extend/#jQuery-extend-deep-target-object1-objectN
        $.extend(true, datatableOpts,
            {
                // "processing": true,
                // "serverSide": true,
                // "ajax": {
                //     "url": resturl,
                //     "dataSrc": ""
                // },
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
                    "sProcessing": "loading data...",
                    "processing": "Подождите...",
                    "search": "Поиск:",
                    "lengthMenu": "Показать _MENU_ записей",
                    "info": "Всего _TOTAL_ записей",
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
    //Обновление суммы Сертификатов
    // setTimeout(function() {
    //     ctx.updateTable();
    //     var summary = ctx.datatableApi.column(1).data().sum();
    //     $('#summ').html(summary);
    // }, 10);

    // var summary = ctx.datatableApi.column(1).data().sum();
    // $('#summ').html(summary);
}

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// $(function () {
//     makeEditable({
//         "columns": [
//             {"data": "fullname"},
//             {
//                 "data": "createdate",
//                 "render": function (date, type, row) {
//                         return moment(date).format("DD.MM.YYYY");
//                 }
//             },
//             {
//                 "data": "workdate",
//                 "render": function (date, type, row) {
//                     return moment(date).format("DD.MM.YYYY");
//                 }
//             },
//             {"data": "docFullname"},
//             {"data": "phone1"}
//         ],
//         "order": [
//             [
//                 0,
//                 "asc"
//             ]
//         ]
//     });
//     $('#datatable_info').css('font-weight', 'bold');
// });


function updateTableByData(data) {
    ctx.datatableApi.clear().rows.add(data).draw();
}


/*
*
* Credits to https://css-tricks.com/long-dropdowns-solution/
*Выпадающее меню
*/

var maxHeight = 400;

$(function () {

    $(".dropdown > li").hover(function () {

        var $container = $(this),
            $list = $container.find("ul"),
            $anchor = $container.find("a"),
            height = $list.height() * 1.1,       // make sure there is enough room at the bottom
            multiplier = height / maxHeight;     // needs to move faster if list is taller

        // need to save height here so it can revert on mouseout
        $container.data("origHeight", $container.height());

        // so it can retain it's rollover color all the while the dropdown is open
        $anchor.addClass("hover");

        // make sure dropdown appears directly below parent list item
        $list
            .show()
            .css({
                paddingTop: $container.data("origHeight")
            });

        // don't do any animation if list shorter than max
        if (multiplier > 1) {
            $container
                .css({
                    height: maxHeight,
                    overflow: "hidden"
                })
                .mousemove(function (e) {
                    var offset = $container.offset();
                    var relativeY = ((e.pageY - offset.top) * multiplier) - ($container.data("origHeight") * multiplier);
                    if (relativeY > $container.data("origHeight")) {
                        $list.css("top", -relativeY + $container.data("origHeight"));
                    }
                    ;
                });
        }

    }, function () {

        var $el = $(this);

        // put things back to normal
        $el
            .height($(this).data("origHeight"))
            .find("ul")
            .css({top: 0})
            .hide()
            .end()
            .find("a")
            .removeClass("hover");

    });

});