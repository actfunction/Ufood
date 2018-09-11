function btnClick() {

//	var param = {"a":1, "b":2, "c":3};
	var data = $("#setDataIpt").val();
	try {
		debugger;
		data = jQuery.parseJSON(data);
	} catch (e) {
		alert("格式不对，出错啦！！！");
		return false;
	} 
	FireFly.doAct("httpAgent", "agent", data, setData, true);
}

function setData(resData) {
	$("#setDataBody").html("");
	var setDataCon = new Array();
	for (var item in resData) {
		setDataCon.push("<tr><td class=\"borderClass\">" + item + "</td>");
		setDataCon.push("<td class=\"borderClass\">" + resData[item] + "</td></tr>");
	}
	$("#setDataBody").html(setDataCon.join(""));
}