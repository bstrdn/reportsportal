




// document.getElementById('startDate').value = "2020-01-01";

var ctx = {
    ajaxUrl: resturl,
    updateTable: function () {

        makeEditable({
            "columns": [
                {

                    "sType": "ruDate",
                    "data": "dat",
                    "width": "10%",
                    // "render": function (date, type, row) {
                    //     return moment(date).format("DD.MM.YYYY");
                    // },

                },
                {"data": "doc",
                    "width": "30%"
                },
                {"data": "org",
                    // "width": "44%"
                },
                {"data": "deb",
                    "render": function (date, type, row) {
                    if (date === 0) {
                        return ""
                    }
                        return date;
                    }
                    // "width": "10%"
                },
                {"data": "cred",
                    "render": function (date, type, row) {
                        if (date === 0) {
                            return ""
                        }
                        return date;
                    }
                    // "width": "10%"
                },
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
                orgId: $('#legalEntitiesWithId').val(),
                // registrar: $('#allRegistrarWithId').val()
            },
        }).done(updateTableByData);

    }
};




function createAkt(){

}
