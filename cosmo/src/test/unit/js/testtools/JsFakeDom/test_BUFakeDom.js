// This is for explicit standalone testing of BUFakeDom.js
// This file should not rely on jsunit, burst, or anything else.

function tst_BUFakeDom_all() {
  var div1 = document.body.appendChild(document.createElement('div'));
  var idval = 'id_test_BUFakeDom';
  div1.setAttribute('id', idval);
  var found1 = document.getElementById(idval);
}

// go ahead and call it
tst_BUFakeDom_all();
