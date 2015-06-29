<?php
header ( "Content-Type:text/html;charset=utf-8" );
$count = 0;
if (isset ( $_GET ["count"] )) {
	$count = $_GET ["count"]; // utf编码
}
if ($count < 0 || $count > 4) {
	$count = 0;
}
$url = "http://search.sina.com.cn/?t=news";
$match = "/<a href=(.*)?from=index_hotword(.*)?<\/a>/";
$list_web2 = getWebHtml ( $url, $match );
$quit = true;
while ( $quit ) {
	$mycontent = $list_web2 [0] [$count];
	$backresult = replaceall ( $mycontent );
	// $backresult = iconv ( "gb2312", "utf-8//IGNORE", $backresult );
	$backresult = iconv ( "utf-8", "gb2312//IGNORE", $backresult );
	$content = urlencode ( $backresult );
	
	// 转为gb2312编码
	$content = iconv ( "utf-8", "gb2312//IGNORE", $content );
	// 获取新闻url
	$url = "http://search.sina.com.cn/?q=" . $content . "&range=title&c=news&sort=time";
	// 正则表达式匹配获取新闻链接
	$pattern = "/<h2><a href=\"(.*?)\" target=\"_blank\">(.*)?<\/h2>/";
	// 获取新闻链接数组
	$list = getWebHtml ( $url, $pattern );
	
	$match1 = "图集:";
	$match2 = "视频:";
	$match3 = "微博推荐";
	$backresult = "";
	$length = count ( $list [1] );
	for($i = 0; $i < $length; $i ++) // 遍历找到合适的新闻链接
	{
		// 获取新闻标题
		$title = compress_html ( $list [0] [$i] );
		// 获取新闻内容
		$Htmlcontent = file_get_contents ( $list [1] [$i] );
		$Htmlcontent = iconv ( "gb2312", "utf-8//IGNORE", $Htmlcontent );
		if (! strstr ( $title, $match1 ) && ! strstr ( $title, $match2 ) && strstr ( $Htmlcontent, $match3 )) {
			// 找到合适的新闻链接
			$content_pattern = "/id=\"artibody\">(.*)?<div class=\"wb_rec\"/";
			$weblist = getWebHtmlByMatch ( $Htmlcontent, $content_pattern );
			$newlength = count ( $weblist [0] );
			for($j = 0; $j < $newlength; $j ++) {
				// echo $weblist[0][0]."<br>";
				$nohtml2 = compress_html ( $weblist [1] [0] );
				
				$backresult = replaceall ( $nohtml2 );
				// $backresult = strip_tags($nohtml2);
				break;
			}
			break;
		}
	}
	if($backresult==""&&$count<5)
	{
		$count++;
		$quit=true;
	}
	else 
		$quit=false;
}
$match_str = "/（原标题：(.*)?）/";
$info = getContent ( $match_str, $backresult );
$mybackresult = str_replace ( $info, "", $backresult );
if ($mybackresult != "")
	echo $count.$mybackresult;
	//echo $count. substr($mybackresult,0,200);
else
	echo "9";
	// echo $backresult;
	// 获取多余字符
function getContent($strpattern, $str) {
	$match_temp = array ();
	preg_match ( $strpattern, $str, $match_temp );
	if ($match_temp) {
		return $match_temp [0];
	}
}
// 根据正则表达式获取网页内容
function getWebHtml($strUrl, $matchList) {
	if (! $strUrl)
		return false;
		// 获取网页内容
	$strHtml = file_get_contents ( $strUrl );
	// 转为utf-8编码
	$strHtml = iconv ( "gb2312", "utf-8//IGNORE", $strHtml );
	
	$list = array ();
	$match_temp = array ();
	+ preg_match_all ( $matchList, $strHtml, $match_temp );
	if ($match_temp) {
		$list = $match_temp;
	}
	// echo $list;
	return $list;
}
function getWebHtmlByMatch($webstr, $matchList) {
	if (! $webstr)
		return false;
	$strHtml = compress_html ( $webstr );
	// echo $strHtml;
	$list = array ();
	$match_temp = array ();
	+ preg_match_all ( $matchList, $strHtml, $match_temp );
	if ($match_temp) {
		$list = $match_temp;
	}
	// echo $list;
	return $list;
}

/**
 * 压缩html : 清除换行符,清除制表符,去掉注释标记
 *
 * @param
 *        	$string
 * @return 压缩后的$string
 *
 */
function compress_html($string) {
	$string = str_replace ( "\r\n", '', $string ); // 清除换行符
	$string = str_replace ( "\n", '', $string ); // 清除换行符
	$string = str_replace ( "\t", '', $string ); // 清除制表符
	$pattern = array (
			"/> *([^ ]*) *</", // 去掉注释标记
			"/[\s]+/",
			"/<!--[^!]*-->/",
			"/\" /",
			"/ \"/",
			"'/\*[^*]*\*/'" 
	);
	$replace = array (
			">\\1<",
			" ",
			"",
			"\"",
			"\"",
			"" 
	);
	return preg_replace ( $pattern, $replace, $string );
}
function replaceall($string) { // 替换所有的js html css
	$search = array (
			"'<script[^>]*?>.*?</script>'si", // 去掉 javascript
			"'<style[^>]*?>.*?</style>'si", // 去掉 css
			"'<[/!]*?[^<>]*?>'si", // 去掉 HTML 标记
			"'<!--[/!]*?[^<>]*?>'si", // 去掉 注释 标记
			"'([rn])[s]+'", // 去掉空白字符
			"'&(quot|#34);'i", // 替换 HTML 实体
			"'&(amp|#38);'i",
			"'&(lt|#60);'i",
			"'&(gt|#62);'i",
			"'&(nbsp|#160);'i",
			"'&(iexcl|#161);'i",
			"'&(cent|#162);'i",
			"'&(pound|#163);'i",
			"'&(copy|#169);'i",
			"'&#(d+);'e" 
	); // 作为 PHP 代码运行null
	$replace = array (
			"",
			"",
			"",
			"",
			"\1",
			"\"",
			"&",
			"<",
			">",
			" ",
			chr ( 161 ),
			chr ( 162 ),
			chr ( 163 ),
			chr ( 169 ),
			"chr(\1)" 
	);
	return preg_replace ( $search, $replace, $string );
}