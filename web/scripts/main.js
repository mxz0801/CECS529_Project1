    (function (){
    var oSearch = document.getElementById('search-btn'),
    oExit = document.getElementById('exit-btn-search'),
    oGoTo = document.getElementById('goto-btn'),
    oExit1 = document.getElementById('exit-btn'),
    oStem = document.getElementById('stem-btn'),
    oVocab = document.getElementById('vocab-btn'),
    oDirForm = document.getElementById('Directory-form'),
    oSearchForm = document.getElementById('Search-form'),
    oDir = document.getElementById('dir-btn'),
    oDirValue = document.getElementById('dir'),
    oTerm = document.getElementById('term'),
    oDisplay = document.getElementById('display-form'),
    oTitle = document.getElementById('item-list'),
    oSearchResult = document.getElementById('Search-result'),
    oError = document.getElementById('Directory-error');
    var DirValue = 'corpus';
    var printout = '';
    var container = $create('div',{id:'container'});



        function init(){
    validateSession();
    bindEvent();
}

    function validateSession(){
    showOrHideElement(oSearchForm,'none');
    showOrHideElement(oDisplay,'none');

    }

    function showOrHideElement(ele,style){
    ele.style.display=style;
}


    function bindEvent(){
    oGoTo.addEventListener('click', function (){
    DirValue = oDirValue.value;
    if(DirValue ===''){
    oError.innerHTML = 'Please enter your directory'
    return
    }
    else{
        oError ='';
    }
    showOrHideElement(oSearchForm,'block');
    showOrHideElement(oDisplay,'none');
    showOrHideElement(oDirForm,'none');
},false);


    oDir.addEventListener('click', function (){
    showOrHideElement(oSearchForm,'none');
    showOrHideElement(oDisplay,'none');
    showOrHideElement(oDirForm,'block');
},false);

    oExit.addEventListener('click', function (){
        window.close();
},false);

    oExit1.addEventListener('click', function (){
    window.close();
},false);

    oSearch.addEventListener('click', function (){
        oTitle.innerHTML='';
        var elem = document.getElementById("container");
        if(elem){
            elem.remove();
        }
        var SearchTermTep = oTerm.value;
        var SearchTerm = SearchTermTep.replace("+","%2B");

    // if(SearchTerm ===''){
    // oSearchError.innerHTML = 'Please enter your word'
    // return
    // }else{
    //     oSearchError='';
    // }
    var opt = {
    method: 'GET',
    url: './search?dir=' + DirValue + '&input='+SearchTerm,
}
        // ajax({
        //     method: opt.method,
        //     url: opt.url,
        //     success:function (res){
        //         var list = '';
        //         for(var i=0;i<res.length;i++){
        //             printout = res[i].title+"<br />";
        //             list +=printout;
        //             oTitle.innerHTML = list;
        //         }
        //
        //     }
        // })
        ajax({
            method: opt.method,
            url: opt.url,
            success:function (res){
                var list = '',
                    item;
                len = res.length;
                if(len>0){
                    for(var i=0;i<len;i++){
                        // item = res[i].title;
                        // list +=item;
                        addItem(oTitle,res[i],i)
                    }
                    oSearchResult.innerText='Total Documents: ' +len;
                    window.printf="Total Documents: " +len;
                }
                else{
                oTitle.innerHTML = 'Not found';

                }
            }
        })

        container = $create('div',{id:'container'})
        showOrHideElement(oSearchForm,'block');
    showOrHideElement(oDisplay,'block');
    showOrHideElement(oDirForm,'none');

    },false);


    oStem.addEventListener('click', function (){
        oTitle.innerHTML='';
        var SearchTerm = oTerm.value;
        // if(SearchTerm ===''){
        //         oSearchError.innerHTML = 'Please enter your word'
        //         return
        //     }
        // else{
        //     oSearchError='';
        // }
            var opt = {
                method: 'GET',
                url: './stem?dir=' + DirValue + '&input='+SearchTerm,
            }
        ajax({
            method: opt.method,
            url: opt.url,
            success:function (res){

                    printout = res
                    oTitle.innerHTML = printout;
                }


        })
            showOrHideElement(oSearchForm,'block');
            showOrHideElement(oDisplay,'block');
            showOrHideElement(oDirForm,'none');
    },false);

        oVocab.addEventListener('click', function (){
            oTitle.innerHTML='';
            var SearchTerm = oTerm.value;
            // if(SearchTerm ===''){
            //     oSearchError.innerHTML = 'Please enter your word'
            //     return
            // }else{
            //     oSearchError='';
            // }
            var opt = {
                method: 'POST',
                url: './search?dir=' + DirValue + '&input='+SearchTerm,
            }
            ajax({
                method: opt.method,
                url: opt.url,
                success:function (res){
                        printout = res;
                        oTitle.innerHTML = printout;
                    }
            })
            showOrHideElement(oSearchForm,'block');
            showOrHideElement(oDisplay,'block');
            showOrHideElement(oDirForm,'none');
        },false);




}


    function addItem(itemList,item,i){
        var section = $create('div', {className: 'item-title'});
        var show = $create('button',{id:'btn'+i});
        var body = $create('p',{id: 'content'+i, style:'display:none'})
        body.innerHTML = item.body;
        var title = $create('a',{id:'link'+i, href:item.url,target:'_blank'});
        title.innerHTML = item.title;
        section.appendChild(title);
        section.appendChild(show);
        section.appendChild(body);

        container.appendChild(section);
        oTitle.append(container);


    }



        function $create(tag, options) {
            var element = document.createElement(tag);
            for (var key in options) {
                if (options.hasOwnProperty(key)) {
                    element[key] = options[key];
                }
            }
            return element;
        }
    /**
     * AJAX helper
     */
    function ajax(opt){
    var opt = opt ||{},
    method = (opt.method||'GET').toUpperCase(),
    url=opt.url,
        success = opt.success || function(){},
    //step1: create
    xhr = new XMLHttpRequest();

    //error checking
    if(!url){
    throw new Error('missing url');
}

    //step2:configuration
    xhr.open(method,url,true);

    //step 3: send
    xhr.send();

    //step4: listen
    //case1: success
    xhr.onload=function (){
    //check response
        success(JSON.parse(xhr.responseText));


}
    //case2: fail
    xhr.onerror = function (){
    throw new Error('The request could not be completed.')
}

}

    init()
})()
