// var reportName = $('#reportName').val()
// var firstResturl = 'rest/' + reportName;
// var resturl = 'rest/report_buh_1?reportName=report_buh_1&startDate=2020-01-01&endDate=2020-12-31&sertId=0';


// var startdate = "2020-01-01"
// document.getElementById('startDate').value = "2020-01-01";

var ctx = {
    ajaxUrl: resturl,
    updateTable: function () {

        makeEditable({
            "columns": [
                {"data": "fullname",
                    "width": "27%"},
                {"data": "date_reg",
                    "width": "8%"},
                {"data": "summ",
                    "width": "6%"},
                {"data": "n_saldo",
                    "width": "6%"},
                {"data": "dates_pay",
                    "width": "39%"},
                {"data": "r_amountrub",
                    "width": "7%"},
                {"data": "k_saldo",
                    "width": "7%"},
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
            url: resturl,
            data: {
                reportName: reportName,
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                // radio: getRadio(),
                sertId: $('#allCertificate').val(),
                // registrar: $('#allRegistrarWithId').val()
            },
        }).done(updateTableByData);

        // debugger;
        // var summary = ctx.datatableApi.column(1).data().sum();
        // $('#summ').html(summary);

        setTimeout(function () {
            var summIsh = ctx.datatableApi.column(6).data().sum();
            $('#summIsh').html(summIsh);
            var summRs = ctx.datatableApi.column(5).data().sum();
            $('#summRs').html(summRs);
            var summVh = ctx.datatableApi.column(3).data().sum();
            $('#summVh').html(summVh);
        }, 1000);

        setTimeout(function () {
            var summIsh = ctx.datatableApi.column(6).data().sum();
            $('#summIsh').html(summIsh);
            var summRs = ctx.datatableApi.column(5).data().sum();
            $('#summRs').html(summRs);
            var summVh = ctx.datatableApi.column(3).data().sum();
            $('#summVh').html(summVh);
        }, 3000);

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


