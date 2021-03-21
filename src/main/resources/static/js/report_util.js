
function makeEditable(datatableOpts) {
    ctx.datatableApi = $("#datatable").DataTable(
        // https://api.jquery.com/jquery.extend/#jQuery-extend-deep-target-object1-objectN
        $.extend(true, datatableOpts,
            {
                // "processing": true,
                // "serverSide": true,
                // "ajax": {
                //     "url": resturl,
                //     "dataSrc": ""
                // },


                // "bPaginate": false,
                // "bSortClasses":false,
                // asStripClasses:[],
                // stripeClasses: [],
                "deferRender": true,
                stateSave: true,
                retrieve: true,
                destroy: true,
                "iDisplayLength": 20,
                "aLengthMenu": [[10, 20, 50, 100, -1], [10, 20, 50, 100, "Все"]],
                "dom": '<"dt-buttons"Bfli>rtp',
                "paging": true,
                "autoWidth": true,
                "fixedHeader": true,
                "buttons": [
                    'pdf',
                    'excelHtml5',
                    'copyHtml5',
                    'colvis',
                    {
                        extend: 'print',
                        text: 'Печать',
                        autoPrint: true,
                        exportOptions: {
                            columns: ':visible',
                        },
                        customize: function (win) {
                            $(win.document.body).find('table').addClass('display').css('font-size', '9px');
                            $(win.document.body).find('tr:nth-child(odd) td').each(function (index) {
                                $(this).css('background-color', '#D0D0D0');
                            });
                            $(win.document.body).find('h1').css('text-align', 'center');
                        }
                    }
                ],
                language: {
                    "sProcessing": "loading data...",
                    "processing": "Подождите...",
                    "search": "Поиск:",
                    "lengthMenu": "Показать _MENU_ записей",
                    "info": "Всего _TOTAL_ записей",
                    // "info": "Пациенты с _START_ по _END_ из _TOTAL_ пациентов",
                    "infoEmpty": "Записи с 0 до 0 из 0 записей",
                    "infoFiltered": "(отфильтровано из _MAX_ записей)",
                    "infoPostFix": "",
                    "loadingRecords": "Загрузка записей...",
                    "zeroRecords": "Записи отсутствуют.",
                    "emptyTable": "В таблице отсутствуют данные",
                    "paginate": {
                        "first": "Первая",
                        "previous": "Предыдущая",
                        "next": "Следующая",
                        "last": "Последняя"
                    },
                    "aria": {
                        "sortAscending": ": активировать для сортировки столбца по возрастанию",
                        "sortDescending": ": активировать для сортировки столбца по убыванию"
                    },
                    // stripeClasses: [],
                    // asStripClasses: []
                }
            }
        ));
}




