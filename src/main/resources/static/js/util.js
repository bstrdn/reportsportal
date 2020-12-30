



//TODO сделать обрезуку ФИО, чтобы была фамилия + инициалы
$(document).ready(function() {
    // var n = document.getElementById("all_registrar").
    // document.getElementById("all_registrar").options[1].text = document.getElementById("all_registrar").options[1].text.replace(/(.+) (.).+ (.).+/, '$1 $2. $3.');
    // document.getElementById("all_registrar").options.each
    var select = document.getElementsByName('design_tag_type_2')[0];
    var options = select.getElementsByTagName('option');
    for (var i=0; i<options.length; i++)  {
        alert(options[i].value);
        // let source = 'Васильев Иван Петрович';
// let result = source.replace(/(.+) (.).+ (.).+/, '$1 $2. $3.');
// console.log(result);
//
//         function showMessage(text) { // аргументы: from, text
//             alert(text);
//         }
    // var select = document.getElementsByName('all_registrar')[0];
    // var options = select.getElementsByTagName('option');
    // for (var i=0; i<options.length; i++)  {
    //     alert(options[i].value);
    }
    // var t = document.getElementById("all_registrar").options[1].text;
    //
    // alert(t);
    //     n1.replace(/(.+) (.).+ (.).+/, '$1 $2. $3.');
    // var n2 = document.getElementById("all_registrar").options[1].value;
    // n2.replace(/(.+) (.).+ (.).+/, '$1 $2. $3.');

    <!--<select name="design_tag_type_2">-->

    // <!--    <option id="tag_1" value="test1">test1</option>-->
    //
    // <!--    <option id="tag_2" value="test2">test2</option>-->
    //
    // <!--    <option id="tag_3" value="test3">test3</option>-->
    //
    // <!--</select>-->
});