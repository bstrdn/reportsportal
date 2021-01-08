var date = new Date();
var day = date.getDate(),
    month = date.getMonth() + 1,
    year = date.getFullYear(),
    month = (month < 10 ? "0" : "") + month;
day = (day < 10 ? "0" : "") + day;
var today = year + "-" + month + "-01";
document.getElementById('endDate').value = today;
var lastMonth = (month == 1 ? year-1 : year) + "-" + (month == 1 ? 12 : month-1) + "-01";
document.getElementById('startDate').value = lastMonth;


// console.log(lastMonth);
//
// function updateTableByData(data) {
//     ctx.datatableApi.clear().rows.add(data).draw();
// }

// var startDate = $('#startDate');
// var endDate = $('#endDate');
// var endDate = $("#endDate").value;
// $(document).ready(function() {
// startDate =
// });

// $.ajaxSetup({
//     converters: {
//         "text json": function (stringData) {
//             var json = JSON.parse(stringData);
//             if (typeof json === 'object') {
//                 $(json).each(function () {
//                     if (this.hasOwnProperty('dateTime')) {
//                         this.dateTime = this.dateTime.substr(0, 16).replace('T', ' ');
//                     }
//                 });
//             }
//             return json;
//         }
//     }
// });

// var restParams = new Array();
// restParams.push({"name" : "limit", "value" : 112});
// restParams.push({"name" : "page", "value" : 123 });
// restParams.push({"name" : "sort", "value" : "sdfsdf" });


//РАБОЧАЯ ТАБЛИЦА
// var table = $('#datatable').dataTable( {
//     stateSave: true,
//     retrieve: true,
//     destroy: true,
//     "iDisplayLength": 20,
//     "aLengthMenu": [[ 10, 20, 50, 100 ,-1],[10,20,50,100,"Все"]],
//     "dom": '<"dt-buttons"Bfli>rtp',
//     "paging": true,
//     "autoWidth": true,
//     "fixedHeader": true,
//     "buttons": [
//         'pdf',
//         'excelHtml5',
//         'copyHtml5',
//         'colvis',
//         'print'
//     ],
//     language: {
//         "processing": "Подождите...",
//         "search": "Поиск:",
//         "lengthMenu": "Показать _MENU_ записей",
//         "info": "Записи с _START_ до _END_ из _TOTAL_ записей",
//         "infoEmpty": "Записи с 0 до 0 из 0 записей",
//         "infoFiltered": "(отфильтровано из _MAX_ записей)",
//         "infoPostFix": "",
//         "loadingRecords": "Загрузка записей...",
//         "zeroRecords": "Записи отсутствуют.",
//         "emptyTable": "В таблице отсутствуют данные",
//         "paginate": {
//             "first": "Первая",
//             "previous": "Предыдущая",
//             "next": "Следующая",
//             "last": "Последняя"
//         },
//         "aria": {
//             "sortAscending": ": активировать для сортировки столбца по возрастанию",
//             "sortDescending": ": активировать для сортировки столбца по убыванию"
//         }
//     },
//     ajax: {
//         dataType : 'json',
//         type : "GET",
//         url : "rest/report1",
//         data: {
//             startDate: $('#startDate').val(),
//             endDate: $('#endDate').val()
//         },
//         dataSrc: function(json){
//             return json;
//         }
//     },
//     "columns": [
//         { "data": "fullname" },
//         { "data": "fixdate" },
//         { "data": "workdate" },
//         { "data": "docFullname" }
//     ]
// } );


// /* Custom filtering function which will search data in column four between two values */
// $.fn.dataTable.ext.search.push(
//     function( settings, data, dataIndex ) {
//         var min = parseInt( $('#min').val(), 10 );
//         var max = parseInt( $('#max').val(), 10 );
//         var age = parseFloat( data[3] ) || 0; // use data for the age column
//
//         if ( ( isNaN( min ) && isNaN( max ) ) ||
//             ( isNaN( min ) && age <= max ) ||
//             ( min <= age   && isNaN( max ) ) ||
//             ( min <= age   && age <= max ) )
//         {
//             return true;
//         }
//         return false;
//     }
// );
//
// $(document).ready(function() {
//     // Event listener to the two range filtering inputs to redraw on input
//     $('#min, #max').keyup( function() {
//         table.draw();
//     } );
// } );


// $(document).ready(function() {
//     //Only needed for the filename of export files.
//     //Normally set in the title tag of your page.
//     document.title='Simple DataTable';
//     // DataTable initialisation
//     $('#datatable').DataTable(
//         {
//             "dom": '<"dt-buttons"Bfli>rtp',
//             "paging": false,
//             "autoWidth": true,
//             "fixedHeader": true,
//             "buttons": [
//                 'colvis',
//                 'copyHtml5',
//                 'csvHtml5',
//                 'excelHtml5',
//                 'pdfHtml5',
//                 'print'
//             ]
//         }
//     );
// });


function demo() {
    table
        .clear()
        .draw();
    alert("test");

}

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
                radio: getRadio()
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
                    'print'
                ],
                language: {
                    "processing": "Подождите...",
                    "search": "Поиск:",
                    "lengthMenu": "Показать _MENU_ записей",
                    "info": "Пациенты с _START_ по _END_ из _TOTAL_ пациентов",
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
            {"data": "docFullname"}
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

