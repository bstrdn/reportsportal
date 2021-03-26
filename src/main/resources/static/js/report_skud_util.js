
var ctx = {
    ajaxUrl: resturl,
    updateTable: function () {



        makeEditable({
            "columns": [
                {"data": "date",
                    // "width": "27%"
                },
                {"data": "getin",
                    // "width": "8%"
                },
                {"data": "getout",
                    // "width": "6%"
                },
                {"data": "facttime",
                    // "width": "6%"
                },
                {"data": "norma",
                    // "width": "39%"
                },
                {"data": "resulttime",
                    // "width": "7%"
                },
            ],
            "order": [
                [
                    0,
                    "asc"
                ]
            ],
            "createdRow": function (row, data, dataIndex) {
                $(row).attr("data-mealExcess", data.resulttime[0]==='-');
            },
        });
        $('#datatable_info').css('font-weight', 'bold');

        $.ajax({
            type: "GET",
            url: resturl,
            data: {

                reportName: reportName,
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                skudUserId: $('#allSkudUsers').val()
            },
        }).done(updateTableByData);
    }
};
