//
// $(document).ready(function() {
//     alert(document.getElementsByName());
// });

//Обрезка ФИО, фамилия + инициалы
$(document).ready(function() {
    var select = document.getElementsByName('allRegistrar')[0].getElementsByTagName('option');
    for (var i=0; i<select.length; i++) {
        select[i].text = select[i].text.replace(/(.+) (.).+ (.).+/, '$1 $2. $3.');
    }
});