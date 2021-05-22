// var reportName = $('#reportName').val()
// var firstResturl = 'rest/' + reportName;
// var resturl = 'rest/report_buh_1?reportName=report_buh_1&startDate=2020-01-01&endDate=2020-12-31&sertId=0';


// var startdate = "2020-01-01"
// document.getElementById('startDate').value = "2020-01-01";
$('#accountSearchDataTableLoader').hide();
nextMonth();

function updateTableByData(data) {
    $('#accountSearchDataTableLoader').hide();
    // alert("Подождите пока загружается отчет (~8 секунд)")
    ctx.datatableApi.clear().rows.add(data).draw();
    setTimeout(function () {
        var summNach = ctx.datatableApi.column(3).data().sum();
        $('#summNach').html(summNach);
        var summSpis = ctx.datatableApi.column(4).data().sum();
        $('#summSpis').html(summSpis);

    }, 200);
    //здесь сделать подсчет
    // alert("starn");
}


function previousMonth(){
    date.setMonth(date.getMonth() - 1);
    document.getElementById('endDate').value = date.getFullYear() + "-" + getMonthWithO(date.getMonth() + 1) + "-" + daysInMonth(date.getMonth() + 1, date.getFullYear());
}

function nextMonth(){
    date.setMonth(date.getMonth() + 1);
    document.getElementById('endDate').value = date.getFullYear() + "-" + getMonthWithO(date.getMonth() + 1) + "-" + daysInMonth(date.getMonth() + 1, date.getFullYear());
}





var ctx = {
    // ajaxUrl: resturl,
    updateTable: function () {

        makeEditable({
            "columns": [
                {
                    "data": "r3client_name",
                    "width": "15%"
                },
                {
                    "data": "name_doc",
                    "width": "15%"
                },
                {
                    "data": "dolg",
                    "width": "7%"
                },
                {
                    "data": "dolg_res",
                    "width": "7%",
                    // "render": function (data, type, row) {
                    //     return moment(data).format("DD.MM.YYYY");
                    // },
                },
                {
                    "data": "avans",
                    "width": "7%"
                },
                {
                    "data": "data_voznic",
                    "width": "9%",
                    // render: function (data, type, row) {
                    //     if (data > 0) {
                    //         return '<p style="color: red">' + data + '</p>';
                    //     } else {
                    //         return data;
                    //     }
                    // },
                },
            ],
            "order": [
                [
                    0,
                    "asc"
                ]
            ],
            // 'preDrawCallback': function(settings) {
            //
            // },
            // 'drawCallback': function(settings) {
            //     document.getElementById('dataTables_empty').style.backgroundColor = "yellow";
            // },
            "initComplete": function () {
                $('#accountSearchDataTableLoader').show();
            }
            // "createdColumn": function (row, data, dataIndex) {
            //     alert("COMPLETE");            },
        });
        $('#datatable_info').css('font-weight', 'bold');

        $('#accountSearchDataTableLoader').show();

        // alert("tedsgf")

        $.ajax({
            type: "GET",
            url: resturl,
            data: {
                reportName: reportName,
                endDate: $('#endDate').val(),
            },
        }).done(updateTableByData);


        // setTimeout(function () {
        //     var summNach = ctx.datatableApi.column(4).data().sum();
        //     $('#summNach').html(summNach);
        //     var summSpis = ctx.datatableApi.column(5).data().sum();
        //     $('#summSpis').html(summSpis);
        //
        // }, 1000);
        // setTimeout(function () {
        //     var summNach = ctx.datatableApi.column(4).data().sum();
        //     $('#summNach').html(summNach);
        //     var summSpis = ctx.datatableApi.column(5).data().sum();
        //     $('#summSpis').html(summSpis);
        //
        // }, 2000);
        // setTimeout(function () {
        //     var summNach = ctx.datatableApi.column(4).data().sum();
        //     $('#summNach').html(summNach);
        //     var summSpis = ctx.datatableApi.column(5).data().sum();
        //     $('#summSpis').html(summSpis);
        //
        // }, 3000);


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


// ctx.updateTable();