var reportName = $('#reportName').val()
var resturl = 'rest/' + reportName;

var date = new Date();
// date.setMonth(date.getMonth() - 1);
var day = date.getDate();
month = date.getMonth() + 1;
year = date.getFullYear();
year = (month == 1 ? year - 1 : year);
startmonth = (month == 1 ? 12 : month - 1);
var enddate = year + "-" + getMonthWithO(startmonth) + "-" + daysInMonth(startmonth, year);



previousMonth();

function getMonthWithO (month) {
    return (month < 10 ? "0" : "") + month;
}
function getDayWithO (day) {
    return (day < 10 ? "0" : "") + day;
}

function lastMonth() {
    document.getElementById('startDate').value = year + "-" + getMonthWithO(startmonth) + "-" + "01";
    document.getElementById('endDate').value = enddate;


}

function currentMonth() {
    document.getElementById('startDate').value = year + "-" + getMonthWithO(month)  + "-" + "01";
    document.getElementById('endDate').value = year + "-" + getMonthWithO(month)  + "-" + getDayWithO(day);

}

function previousMonth(){
date.setMonth(date.getMonth() - 1);
    document.getElementById('startDate').value = date.getFullYear() + "-" + getMonthWithO(date.getMonth() + 1) + "-01";
    document.getElementById('endDate').value = date.getFullYear() + "-" + getMonthWithO(date.getMonth() + 1) + "-" + daysInMonth(date.getMonth() + 1, date.getFullYear());
}

function nextMonth(){
date.setMonth(date.getMonth() + 1);
    document.getElementById('startDate').value = date.getFullYear() + "-" + getMonthWithO(date.getMonth() + 1) + "-01";
    document.getElementById('endDate').value = date.getFullYear() + "-" + getMonthWithO(date.getMonth() + 1) + "-" + daysInMonth(date.getMonth() + 1, date.getFullYear());
}



function daysInMonth(month, year) {
    return new Date(year, month, 0).getDate();
}

function getRadio() {
    var allradio = document.getElementsByName('radiorep');
    for (var i = 0; i < allradio.length; i++) {
        if (allradio[i].checked) {
            return allradio[i].value;
        }
    }
}

function getFilterCombine() {
    var allradio2 = document.getElementsByName('radio_combine');
    for (var i = 0; i < allradio2.length; i++) {
        if (allradio2[i].checked) {
            return allradio2[i].value;
        }
    }
}





function updateTableByData(data) {
    ctx.datatableApi.clear().rows.add(data).draw();
}




//!! Обрезка ФИО, фамилия + инициалы
// $(document).ready(function() {
//     var select = document.getElementsByName('allRegistrar')[0].getElementsByTagName('option');
//     for (var i=0; i<select.length; i++) {
//         select[i].text = select[i].text.replace(/(.+) (.).+ (.).+/, '$1 $2. $3.');
//     }
// });