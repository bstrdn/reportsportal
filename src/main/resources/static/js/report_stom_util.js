var reportName = $('#reportName').val()
var resturl = 'rest/' + reportName;
var startdate = year + "-" + month + "-01";
document.getElementById('startDate').value = startdate;

var ctx = {
    ajaxUrl: resturl,
    updateTable: function () {
        $.ajax({
            type: "GET",
            url: resturl,
            data: {
                reportName: reportName,
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                radio: getRadio(),
                department: $('#allDepartmentWithId').val(),
                registrar: $('#allRegistrarWithId').val()
            },
        }).done(updateTableByData);
    }
};



$(function () {
    makeEditable({
        "columns": [
            {"data": "fullname"},
            {
                "data": "createdate",
                "render": function (date, type, row) {
                        return moment(date).format("DD.MM.YYYY");
                }
            },
            {
                "data": "workdate",
                "render": function (date, type, row) {
                    return moment(date).format("DD.MM.YYYY");
                }
            },
            {"data": "docFullname"},
            {"data": "phone1"}
        ],
        "order": [
            [
                0,
                "asc"
            ]
        ]
    });
    $('#datatable_info').css('font-weight', 'bold');
});
