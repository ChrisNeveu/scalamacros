package com.schrine.html

abstract class Html {
	def toString: String
	def ++(other: Html): Html
}

trait HtmlSerializable {
	def toHtml: Html
}
