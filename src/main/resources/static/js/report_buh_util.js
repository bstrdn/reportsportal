var reportName = $('#reportName').val()
var firstResturl = 'rest/' + reportName;
var resturl = 'rest/report_buh_1?reportName=report_buh_1&startDate=2020-01-01&endDate=2020-12-31&sertId=0';


var startdate = "2020-01-01"
document.getElementById('startDate').value = startdate;

var ctx = {
    ajaxUrl: firstResturl,
    updateTable: function () {

        makeEditable({
            "columns": [
                {"data": "fullname"},
                {"data": "summ"},
                {"data": "rashod"},
                {"data": "nameCert"},
            ],
            "order": [
                [
                    0,
                    "asc"
                ]
            ],
        });
        $('#datatable_info').css('font-weight', 'bold');


        $.ajax({
            type: "GET",
            url: firstResturl,
            data: {
                reportName: reportName,
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                // radio: getRadio(),
                sertId: $('#allCertificate').val(),
                // registrar: $('#allRegistrarWithId').val()
            },
        }).done(updateTableByData);

        setTimeout(function () {
            var summary = ctx.datatableApi.column(1).data().sum();
            $('#summ').html(summary);
        }, 1000);

    }
};


// $(function () {
//     makeEditable({
//         "columns": [
//             {"data": "fullname"},
//             {"data": "summ"},
//             {"data": "rashod"},
//             {"data": "nameCert"},
//         ],
//         "order": [
//             [
//                 0,
//                 "asc"
//             ]
//         ],
//     });
//     $('#datatable_info').css('font-weight', 'bold');
// });


// setTimeout(function() { ctx.updateTable() }, 1000);


