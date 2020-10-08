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
    oTitle = document.getElementById('title-display'),
    oViewContent = document.getElementById('viewContent'),
    oError = document.getElementById('Directory-error'),
    oSearchError = document.getElementById('Search-error');

    var DirValue = 'corpus';
    var printout = '';

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
        window.open('','_self');
},false);

    oExit1.addEventListener('click', function (){
    window.close();
},false);

    oSearch.addEventListener('click', function (){
    var SearchTerm = oTerm.value;
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
        ajax({
            method: opt.method,
            url: opt.url,
            success:function (res){
                var list = '';
                for(var i=0;i<res.length;i++){
                    printout = res[i].title+"<br />";
                    list +=printout;
                    oTitle.innerHTML = list;
                }

            }
        })
        showOrHideElement(oSearchForm,'block');
    showOrHideElement(oDisplay,'block');
    showOrHideElement(oDirForm,'none');
    showOrHideElement(oViewContent,'block');

    },false);

    oStem.addEventListener('click', function (){
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
            showOrHideElement(oViewContent,'none');
    },false);

        oVocab.addEventListener('click', function (){
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
            showOrHideElement(oViewContent,'none');
        },false);




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
