function btnClick() {

//	var param = {"a":1, "b":2, "c":3};
	var data = $("#setDataIpt").val();
	try {
		data = jQuery.parseJSON(data);
	} catch (e) {
		return false;
		alert("格式不对，出错啦！！！");
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