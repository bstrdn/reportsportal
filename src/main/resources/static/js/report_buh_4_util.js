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
                {
                    // "bUseRendered" : true,
                    // "sType": "date",
                    // type: 'de_date',
                    "data": "data_spis",
                    "width": "6%",
                    // "render": function (data, type, row) {
                    //     return moment(data).format("DD.MM.YYYY");
                    // },

                },
                {
                    "data": "fio_pat",
                    "width": "15%"
                },
                {
                    "data": "fio_doc",
                    "width": "11%"
                },
                {
                    "data": "data_nachisl",
                    "width": "6%",
                    // "render": function (data, type, row) {
                    //     return moment(data).format("DD.MM.YYYY");
                    // },
                },
                {
                    "data": "sum_nachisl",
                    "width": "4%"
                },
                {
                    "data": "sum_spis",
                    "width": "4%",
                    render: function (data, type, row) {
                        if (data > 0) {
                            return '<p style="color: red">' + data + '</p>';
                        } else {
                            return data;
                        }
                    },
                },
                {
                    "data": "data_zn",
                    "render": function (data, type, row) {
                        if (data !== null) {
                            return data;
                            // return moment(data).format("DD.MM.YYYY");
                        } else {
                            return "";
                        }
                    },
                    "width": "7%"
                },
                {
                    type:'html',
                    "data": "code",
                    "width": "12%",
                    "render": function (data, type, row) {
                        return data.replace(/\n/g, "<br>");
                    },

                },
            ],
            "order": [
                [
                    0,
                    "asc"
                ]
            ],
            // "createdColumn": function (row, data, dataIndex) {
            //     $(row).attr("excess", data!==0);
            // },
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
                dcode: $('#allDocWithId').val(),
                // registrar: $('#allRegistrarWithId').val()
            },
        }).done(updateTableByData);

        // debugger;
        // var summary = ctx.datatableApi.column(1).data().sum();
        // $('#summ').html(summary);

        setTimeout(function () {
            var summNach = ctx.datatableApi.column(4).data().sum();
            $('#summNach').html(summNach);
            var summSpis = ctx.datatableApi.column(5).data().sum();
            $('#summSpis').html(summSpis);

        }, 1000);
        setTimeout(function () {
            var summNach = ctx.datatableApi.column(4).data().sum();
            $('#summNach').html(summNach);
            var summSpis = ctx.datatableApi.column(5).data().sum();
            $('#summSpis').html(summSpis);

        }, 2000);
        setTimeout(function () {
            var summNach = ctx.datatableApi.column(4).data().sum();
            $('#summNach').html(summNach);
            var summSpis = ctx.datatableApi.column(5).data().sum();
            $('#summSpis').html(summSpis);

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


