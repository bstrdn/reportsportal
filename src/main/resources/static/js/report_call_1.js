
var ctx = {
    ajaxUrl: resturl,
    updateTable: function () {

        makeEditable({
            // "autoWidth": true,

            "columns": [
                {
                    "data": "name",
                    // "width": "27%"
                },
                {
                    "data": "call_out",
                    // "width": "27%"
                },
                {
                    "data": "call_in",
                    // "width": "18%"
                    // "render": function (date, type, row) {
                    //     return moment(date).format("DD.MM.YYYY");
                    // }

                },
                {
                    "data": "all_call",
                    // "width": "18%"
                    // "render": function (date, type, row) {
                    //     return moment(date).format("DD.MM.YYYY");
                    // }
                },
                {
                    "data": "in_sched",
                    // "width": "24%"
                },
                {
                    "data": "out_sched",
                    // "width": "17%"
                },
                {
                    "data": "procent",
                    "width": "20%",
                    render: function (data, type, row, meta) {
                        return type === 'display' ?
                            '<progress value="' + data + '" max="100"></progress>&nbsp&nbsp' + data + '%' :
                            data;

                    }
                }
            ],
            "order": [
                [
                    6,
                    "asc"
                ]
            ]
        });
        $('#datatable_info').css('font-weight', 'bold');


        $.ajax({
            type: "GET",
            url: resturl,
            data: {
                reportName: reportName,
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                select1: $('#select1').val()
            },
        }).done(updateTableByData);

    }
};

