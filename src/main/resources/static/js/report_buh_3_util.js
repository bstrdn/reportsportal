document.getElementById('startDate').value = "2020-01-01";

var reportName = $('#reportName').val()
var firstResturl = 'rest/' + reportName;


var ctx = {
    ajaxUrl: firstResturl,
    updateTable: function () {

        makeEditable({
            "columns": [
                {"data": "fullname"},
                {"data": "summ"},
                {"data": "rashod"},
                {"data": "name_cert"},
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
                sertId: $('#allCertificate').val(),
            },
        }).done(updateTableByData);

        setTimeout(function () {
            var summary = ctx.datatableApi.column(1).data().sum();
            $('#summ').html(summary);
        }, 1000);

        setTimeout(function () {
            var summary = ctx.datatableApi.column(1).data().sum();
            $('#summ').html(summary);
        }, 3000);

    }
};
