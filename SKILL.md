---
name: sca-chatbot-lite
description: "生成或改写中文聊天、进度回复时使用。抓主要矛盾，把当前前提支持的主判断说到最大程度，消除自证、免责和结论退让。发送前运行内嵌的 Python 标准库检查器检查完整候选；单文件，无第三方包。"
---

# SCA · 说清一切 · Chatbot Lite v0.3

SCA 要约束模型反复出现的自证、免责和给结论留退路的习惯。写作者必须主动抓住主要矛盾，把主判断说足，把用户需要的解释讲清楚。程序用常见措辞拦截兜住表达下限，命中就重新处理正文。以下要求在内部执行，向用户交付解决问题的回答。

## 起草：把主判断说到最大程度

- **主动判断什么最能决定当前问题。** 找出决定答案的主要事实或关系，尽早给出主判断。必须把当前前提能够支持的主判断说到最大程度：支持明确结论时直接给出；支持概率或程度判断时，充分表达所支持的力度。禁止为了规避出错而主动把结论降弱。
- **按主次分配篇幅。** 主判断说足后，再判断例外实际改变了什么。只改变后果形式的例外作为次要补充，禁止拿它与主判断对立。按内容对当前结论、行动和适用范围的影响决定是否展开；禁止平分主次，或用罗列各种可能代替判断。
- **把事实和解释接起来。** 说清事实怎样推出判断，补齐理解和行动所需的原因、条件、例子和取舍。用熟悉的词写完整关系，避免让读者自行补义。需要类比时，选读者熟悉的日常动作，保住关键动作与后果的方向；设定越补越多时直接解释。缺少决定答案的事实，能查明就继续查；确需用户补充时，提出具体问题。
- **把篇幅用于用户的问题。** 报告成果和进度时，讲结果、关键变化及其收益和代价。删除证明自己尽责的检查流水账、无关保持项，以及自行替用户补出的排除项。用户明确质问代理行为时，直接回应被质问的事实。


## 修订：修好判断、重点和解释

命中后，先判断原句回答了用户哪项问题。无关内容删除；有用内容用具体事实、动作和关系重新表达。禁止只换同义词绕过拦截。

主判断被弱化时，把结论补足。全文仍在强调次要事项时，重写主要解释、篇幅和行动安排；仅在开头补一句结论不能算修复。删掉防御性词句后，仍须判断这些问题是否已经解决。

改稿后对照用户问题、被拦原句和完整新稿，补回删改中丢失的答案、原因、条件和解释。用户指出漏答或重点偏移时，在本次正文中完成修复。

## 执行：检查完整候选后发送

1. 首次启用或换用新版 md 时，将唯一的 Python 代码块原样保存为当前可写目录的 `reply_check.py`，替换旧脚本；随后复用，丢失时重新提取。
2. 完成候选后，核对正文已回答用户问题、说足主判断、讲清必要关系，再保存为 UTF-8 文件，例如 `reply.md`。随后执行 `python reply_check.py reply.md`，使用现有 Python 3.9 或更高版本，路径按实际目录填写。
3. `blocked`：按上节修订完整正文，保存后重测。`error`：修复输入或调用错误再运行。本文件的写作要求已做到且结果为 `clear` 时，原样发送受检正文。正文改动后必须重新检查。

检查器只使用 Python 标准库。禁止增加包安装、联网获取规则或其他文件加载作为运行前提。必须传入候选文件路径，禁止无参数调用；不得跳过检查或伪造通过结果。

检查覆盖标题、段落、列表、表格和引用正文，识别常用 Markdown 与内嵌 HTML。代码和链接目标保持原文，链接文字参与检查。禁止把新写的自然语言包装为代码以规避命中。

## 内嵌检查器

```python
#!/usr/bin/env python3
import json
import sys
from pathlib import Path
import html
import re
from html.parser import HTMLParser


_BOUNDARY = "\x01"
_CODE_GAP = "\x00"
_QUOTES = re.compile(r" {0,3}>[ \t]?")
_LIST = re.compile(r"[ \t]*(?:[-+*]|[0-9]{1,9}[.)])[ \t]+(?:\[[ xX]\][ \t]+)?")
_REF = re.compile(r" {0,3}\[([^\]\n]+)\]:[ \t]*.*$")
_FENCE = re.compile(r" {0,3}(`{3,}|~{3,})([^\n]*)$")
_AUTOLINK = re.compile(r"<([A-Za-z][A-Za-z0-9+.-]{1,31}:[^<>\s]*|[^<>\s@]+@[^<>\s@]+\.[^<>\s@]+)>")
_TAG = re.compile(r"</?[A-Za-z][^<>]*>|<!--.*?-->|<![A-Z][^<>]*>|<\?.*?\?>", re.S)


def _quote_prefix(line, limit=None):
    end = depth = 0
    while limit is None or depth < limit:
        match = _QUOTES.match(line, end)
        if not match:
            break
        end, depth = match.end(), depth + 1
    return end, depth


def _reference_id(text):
    return " ".join(text.split()).casefold()


def _ticks_end(text, start, width):
    for match in re.finditer(r"`+", text[start + width:]):
        if len(match[0]) == width:
            return start + width + match.start(), start + width + match.end()
    return None


def _table_cells(text):
    cuts, i = [], 0
    while i < len(text):
        if text[i] == "\\":
            i += 2
            continue
        if text[i] == "`":
            width = len(re.match(r"`+", text[i:])[0])
            end = _ticks_end(text, i, width)
            if end:
                i = end[1]
                continue
            i += width
            continue
        if text[i] == "|":
            cuts.append(i)
        i += 1
    edges = [-1] + cuts + [len(text)]
    cells = [(a + 1, b) for a, b in zip(edges, edges[1:])]
    if cuts and not text[:cuts[0]].strip():
        cells = cells[1:]
    if cuts and not text[cuts[-1] + 1:].strip():
        cells = cells[:-1]
    return cells


def _table_separator(text):
    cells = _table_cells(text)
    return "|" in text and bool(cells) and all(
        re.fullmatch(r"[ \t]*:?-{3,}:?[ \t]*", text[a:b])
        for a, b in cells
    )


def _blocks(markdown):
    lines = markdown.splitlines(keepends=True)
    chunks, refs, pending = [], set(), []
    fence, last_quote, list_indent, table_quote = None, None, 0, None

    def flush():
        if pending:
            chunks.append("".join(pending))
            pending.clear()

    i = 0
    while i < len(lines):
        raw = lines[i].rstrip("\r\n")
        prefix, quote = _quote_prefix(raw)
        if fence:
            char, width, fence_quote, indent = fence
            cut, found = _quote_prefix(raw, fence_quote)
            close = raw[cut:]
            if indent and close.startswith(" " * indent):
                close = close[indent:]
            if found == fence_quote and re.fullmatch(r" {0,3}" + re.escape(char) + "{" + str(width) + r",}[ \t]*", close):
                fence = None
            i += 1
            continue
        body = raw[prefix:]
        if quote != last_quote:
            flush()
            list_indent = 0
            table_quote = None
        last_quote = quote
        if not body.strip():
            flush()
            table_quote = None
            i += 1
            continue
        marker = _LIST.match(body)
        base_indent = 0
        if marker:
            flush()
            list_indent = marker.end()
            base_indent = list_indent
            body = body[marker.end():]
            table_quote = None
        elif list_indent and body.startswith(" " * list_indent):
            base_indent = list_indent
            body = body[list_indent:]
        match = _FENCE.fullmatch(body)
        if match and not (match[1][0] == "`" and "`" in match[2]):
            flush()
            fence = (match[1][0], len(match[1]), quote, base_indent)
            table_quote = None
            i += 1
            continue
        if body.startswith(("    ", "\t")) and not pending:
            flush()
            table_quote = None
            i += 1
            continue
        reference = _REF.fullmatch(body)
        if reference:
            flush()
            refs.add(_reference_id(reference[1]))
            i += 1
            continue
        heading = re.match(r" {0,3}#{1,6}(?:[ \t]+|$)", body)
        if heading:
            flush()
            trimmed = re.sub(r"[ \t]+#+[ \t]*$", "", body[heading.end():])
            chunks.append(trimmed)
            table_quote = None
            i += 1
            continue
        if re.fullmatch(r" {0,3}(?:(?:\*[ \t]*){3,}|(?:-[ \t]*){3,}|(?:_[ \t]*){3,}|=+|-+)[ \t]*", body):
            flush()
            table_quote = None
            i += 1
            continue
        next_separator = False
        if i + 1 < len(lines):
            nxt = lines[i + 1].rstrip("\r\n")
            n_prefix, n_quote = _quote_prefix(nxt)
            next_separator = n_quote == quote and _table_separator(nxt[n_prefix:].lstrip())
        if "|" in body and (next_separator or table_quote == quote):
            flush()
            for left, right in _table_cells(body):
                chunks.append(body[left:right])
            table_quote = quote
            i += 2 if next_separator else 1
            continue
        table_quote = None
        pending.append(body)
        if len(raw) < len(lines[i]):
            pending.append("\n")
        i += 1
    flush()
    return chunks, refs


def _bracket_pairs(text):
    pairs, stack, i = {}, [], 0
    while i < len(text):
        if text[i] == "\\":
            i += 2
            continue
        if text[i] == "`":
            width = len(re.match(r"`+", text[i:])[0])
            end = _ticks_end(text, i, width)
            if end:
                i = end[1]
                continue
            i += width
            continue
        if text[i] == "[":
            stack.append(i)
        elif text[i] == "]" and stack:
            pairs[stack.pop()] = i
        i += 1
    return pairs


def _parenthesis_end(text, start):
    depth, quote, angle, i = 1, None, False, start + 1
    while i < len(text):
        ch = text[i]
        if ch == "\\":
            i += 2
            continue
        if quote:
            if ch == quote:
                quote = None
        elif angle:
            if ch == ">":
                angle = False
        elif ch == "<":
            angle = True
        elif ch in "\"'" and i > start + 1 and text[i - 1].isspace():
            quote = ch
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if not depth:
                return i + 1
        i += 1
    return None


def _markdown_inline(text, refs, depth=0):
    out, pairs, i = [], _bracket_pairs(text), 0

    def inner(left, right):
        if depth < 16:
            return _markdown_inline(text[left:right], refs, depth + 1)
        return text[left:right]

    while i < len(text):
        ch = text[i]
        if ch == "\\" and i + 1 < len(text) and text[i + 1] in r"!\"#$%&'()*+,-./:;<=>?@[\]^_`{|}~":
            out.append(html.escape(text[i + 1], quote=False))
            i += 2
            continue
        if ch == "`":
            width = len(re.match(r"`+", text[i:])[0])
            end = _ticks_end(text, i, width)
            if end:
                out.append(_CODE_GAP)
                i = end[1]
                continue
            out.append(text[i:i + width])
            i += width
            continue
        if ch == "<":
            auto = _AUTOLINK.match(text, i)
            if auto:
                out.append(html.escape(auto[1], quote=False))
                i = auto.end()
                continue
            tag = _TAG.match(text, i)
            if tag:
                out.append(tag[0])
                i = tag.end()
                continue
        left = i + 1 if text.startswith("![", i) else i
        if left < len(text) and text[left] == "[" and left in pairs:
            right, end = pairs[left], None
            after = right + 1
            if after < len(text) and text[after] == "(":
                end = _parenthesis_end(text, after)
            elif after in pairs:
                ref_end = pairs[after]
                ref = text[after + 1:ref_end] or text[left + 1:right]
                if _reference_id(ref) in refs:
                    end = ref_end + 1
            elif _reference_id(text[left + 1:right]) in refs:
                end = after
            if end:
                out.append(inner(left + 1, right))
                i = end
                continue
        if ch in "*_~":
            run = re.match(re.escape(ch) + "+", text[i:])[0]
            begin = i + len(run)
            can_open = begin < len(text) and not text[begin].isspace()
            if ch == "_" and i and text[i - 1].isalnum() and begin < len(text) and text[begin].isalnum():
                can_open = False
            if ch == "~" and len(run) != 2:
                can_open = False
            if can_open:
                for close in re.finditer(re.escape(ch) + "+", text[begin:]):
                    at = begin + close.start()
                    tail = begin + close.end()
                    if close[0] != run or at == begin or text[at - 1].isspace() or text[at - 1] == "\\":
                        continue
                    if ch == "_" and tail < len(text) and text[tail].isalnum() and text[at - 1].isalnum():
                        continue
                    out.append(inner(begin, at))
                    i = tail
                    break
                else:
                    out.append(run)
                    i = begin
                continue
        out.append(ch)
        i += 1
    return "".join(out)


class _VisibleHTML(HTMLParser):
    _hidden = {"script", "style", "pre", "code", "head", "template"}
    _blocks = {
        "address", "article", "aside", "blockquote", "dd", "details", "dialog",
        "div", "dl", "dt", "fieldset", "figcaption", "figure", "footer", "form",
        "h1", "h2", "h3", "h4", "h5", "h6", "header", "hgroup", "hr", "li",
        "main", "nav", "ol", "p", "section", "summary", "table", "tbody", "td",
        "tfoot", "th", "thead", "tr", "ul",
    }

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.output = []
        self.hidden = []

    def handle_data(self, data):
        if not self.hidden:
            self.output.append(data)

    def handle_starttag(self, tag, attrs):
        if tag in self._hidden:
            if not self.hidden:
                self.output.append(_CODE_GAP)
            self.hidden.append(tag)
        elif not self.hidden:
            if tag in self._blocks:
                self.output.append(_BOUNDARY)
            elif tag == "br":
                self.output.append("\n")

    def handle_startendtag(self, tag, attrs):
        if not self.hidden and (tag in self._blocks or tag in self._hidden):
            self.output.append(_CODE_GAP if tag in self._hidden else _BOUNDARY)
        elif not self.hidden and tag == "br":
            self.output.append("\n")

    def handle_endtag(self, tag):
        if tag in self.hidden:
            del self.hidden[len(self.hidden) - 1 - self.hidden[::-1].index(tag):]
            if not self.hidden:
                self.output.append(_CODE_GAP)
        elif not self.hidden and tag in self._blocks:
            self.output.append(_BOUNDARY)


def prose_units(markdown):
    chunks, refs = _blocks(markdown)
    transformed = "".join(
        _BOUNDARY + _markdown_inline(text, refs) for text in chunks if text
    )
    if not transformed:
        return
    parser = _VisibleHTML()
    parser.feed(transformed)
    parser.close()
    # Inline code remains a hard boundary even if the caller removes Han whitespace.
    for text in "".join(parser.output).split(_BOUNDARY):
        text = text.strip()
        if text.strip(_CODE_GAP).strip():
            yield text


_SHARED_PREFIX = '不足以(?:(?:直接|充分|完全|独立|单独|可靠|准确|明确|完整|全面|初步|大致|进一步|据此|暂时|就)(?:地)?)*(?:在[^，,；;]*(?:内|下)(?:(?:直接|充分|完全|独立|单独|可靠|准确|明确|完整|全面|初步|大致|进一步|据此|暂时|就)(?:地)?)*)?'
PATTERNS = tuple(map(re.compile, (
    '(?:(?:不能|无法|没法|难以|不宜|不应|不可)(?:仅仅|单单|仅|只|单|光)(?:凭借|根据|依据|凭|靠)|(?:仅仅|单单|仅|只|单|光)(?:凭借|根据|依据|凭|靠)[^，,。！？!?；;\\n]*?[，,]?\\s*(?:还|仍|也|尚)?(?:不能|无法|没法|难以|不宜|不应|不可)(?:直接|准确|可靠|明确|轻易|贸然|就|便)*(?:判断|断定|认定|判定|确定|推断|确认|得出))',
    '(?:不|并非|无需|无须)[^，,；;]*[，,；;]\\s*(?:也|亦|更)(?:不|并非|无需|无须)',
    '(?:不只是|不仅(?:仅)?是|不止是).*?更是',
    '(?:不是|并非|并不是).*?而是',
    '(?:不能|不宜)替代(?:具体判断|个案分析|专业判断|进一步验证|直接证据|独立证据|人工判断)',
    '(?:不能|不宜)直接(?:据此)?(?:确认|认定|断定|判定|说明|证明|判断|推断|得出|解释|视为|看作|归因|外推|泛化|概括)',
    '(?:不能|不宜)简单(?:地)?(?:认为|认定|判断|理解|解释|归因|概括|推断|得出|视为|看作|等同)',
    '(?:不能|不宜|不应|不可)(?:将|把)?(?:这一?|上述|该)?(?:结论|结果|经验|规律|样本|观察|证据|判断)[^。！？!?；;\\n]{0,12}(?:推广|外推|泛化|扩展|延伸)',
    '(?:不能|无法|没法|难以|不敢)[^。！？!?，,；;\\n]*?(?:保证|承诺|担保)|(?:不|未)(?:(?:作出|做出|给出|提供|作|做|予以)?(?:任何|绝对|完全)?)(?:保证|承诺|担保)',
    '(?:仍|还|尚)(?:有|存在)?[^，。！？!?；;\\n]{0,12}(?:未知|待验证|待确认|待确定)',
    '(?:会|是|能|可以|足以|有)[^。！？!?；;\\n]*?(?:但是|不过|然而|可是|但|却)[，, \\t]*(?:(?:也|仍然|仍|还|其实)[，, \\t]*)?(?:并非|不一定|未必|不见得)',
    '(?:但是|不过|然而|可是|但|却).*?(?:(?:只|仅)(?:能够|能|可以|可)?|不能|无法|没法|未能|没能|不足以|难以)(?:(?:直接|充分|完全|独立|单独|可靠|准确|明确|完整|全面|初步|大致|进一步|据此|暂时|就)(?:地)?|在[^，,；;]*?(?:内|下))*(?:(?:确认|认定|断定|判定|说明|证明|判断|推断|回答|支撑|支持|得出|解释|视为|当成)|(?:把|将)[^，,；;]+?(?:认定|断定|判定|视为|看作|当成|当作|解释为|判断为|归为|作为))',
    '(?:是否|有无)[^，。！？!?；;\\n]{0,20}(?:还|仍|尚)?(?:没|未)(?:确定|确认|验证)',
    '(?:结论|结果|经验|规律|样本|观察|证据|判断|现象)[^。！？!?；;\\n]{0,16}(?:不能|不宜|不应|不可)(?:推广|外推|泛化|扩展|延伸)',
    '下结论还为时过早',
    '不作普遍化解释',
    '不作更大范围推断',
    '不作更强判断',
    '不作绝对化解释',
    '不作过度延伸',
    '不做无条件结论',
    '不具排他性',
    '不可外推到',
    '不可据此',
    '不可推广到',
    '不可视为',
    '不存在单一答案',
    '不存在统一结论',
    '不宜一刀切',
    '不宜一概而论',
    '不宜上升为',
    '不宜下结论',
    '不宜做绝对化判断',
    '不宜外推',
    '不宜定性',
    '不宜定论',
    '不宜延伸',
    '不宜当成',
    '不宜扩大解释',
    '不宜把.*?说成',
    '不宜把.*?读成',
    '不宜把话说死',
    '不宜据此',
    '不宜提升为',
    '不宜概括为',
    '不宜理解为',
    '不宜由此',
    '不宜看作',
    '不宜笼统',
    '不宜脱离具体条件',
    '不宜脱离具体语境',
    '不宜视为',
    '不宜解读为',
    '不宜说满',
    '不宜过度',
    '不应外推',
    '不应延伸',
    '不应把.*?表述为',
    '不应据此',
    '不应提升为',
    '不应理解为',
    '不应看作',
    '不应视为',
    '不应解读为',
    '不应过度解读',
    '不必然意味着',
    '不把话说满',
    '不排除',
    '不能一刀切',
    '不能一概回答',
    '不能一概套用',
    '不能一概而定',
    '不能一概而论',
    '不能上升为',
    '不能从现象反推动机',
    '不能从结果反推前提',
    '不能从结果反推原因',
    '不能从结果反推过程',
    '不能从表现反推机制',
    '不能代表',
    '不能做绝对化判断',
    '不能充分证明',
    '不能充分说明',
    '不能反推',
    '不能唯一归因',
    '不能完全',
    '不能延伸为',
    '不能归纳为普遍规律',
    '不能当成',
    '不能扩大解释',
    '不能把.*?当',
    '不能把.*?推广到',
    '不能把.*?混为一谈',
    '不能把.*?说成',
    '不能把话说死',
    '不能把这一层面的结论扩大到另一层面',
    '不能据',
    '不能排他',
    '不能排除',
    '不能提升为',
    '不能断言',
    '不能无限外推',
    '不能机械套用',
    '不能概括为',
    '不能照搬',
    '不能理解为',
    '不能由此',
    '不能看作',
    '不能确保',
    '不能笼统',
    '不能等同处理',
    '不能给出确定答复',
    '不能给出绝对答案',
    '不能自动(?:推出|得出|说明|证明|转化为|升级为|等同于|视为|归因于)',
    '不能视为',
    '不能解读为',
    '不能证明',
    '不能说明',
    '不能说满',
    '不能锁定唯一',
    '不要仅凭',
    '不要把.*?当作',
    '不要把.*?理解成',
    '不要据此',
    _SHARED_PREFIX + '判定',
    _SHARED_PREFIX + '判断',
    _SHARED_PREFIX + '回答',
    _SHARED_PREFIX + '将(?:[^，,；;])+?作为',
    _SHARED_PREFIX + '将(?:[^，,；;])+?判定',
    _SHARED_PREFIX + '将(?:[^，,；;])+?判断为',
    _SHARED_PREFIX + '将(?:[^，,；;])+?归为',
    _SHARED_PREFIX + '将(?:[^，,；;])+?当作',
    _SHARED_PREFIX + '将(?:[^，,；;])+?当成',
    _SHARED_PREFIX + '将(?:[^，,；;])+?断定',
    _SHARED_PREFIX + '将(?:[^，,；;])+?看作',
    _SHARED_PREFIX + '将(?:[^，,；;])+?视为',
    _SHARED_PREFIX + '将(?:[^，,；;])+?解释为',
    _SHARED_PREFIX + '将(?:[^，,；;])+?认定',
    _SHARED_PREFIX + '当成',
    _SHARED_PREFIX + '得出',
    _SHARED_PREFIX + '把(?:[^，,；;])+?作为',
    _SHARED_PREFIX + '把(?:[^，,；;])+?判定',
    _SHARED_PREFIX + '把(?:[^，,；;])+?判断为',
    _SHARED_PREFIX + '把(?:[^，,；;])+?归为',
    _SHARED_PREFIX + '把(?:[^，,；;])+?当作',
    _SHARED_PREFIX + '把(?:[^，,；;])+?当成',
    _SHARED_PREFIX + '把(?:[^，,；;])+?断定',
    _SHARED_PREFIX + '把(?:[^，,；;])+?看作',
    _SHARED_PREFIX + '把(?:[^，,；;])+?视为',
    _SHARED_PREFIX + '把(?:[^，,；;])+?解释为',
    _SHARED_PREFIX + '把(?:[^，,；;])+?认定',
    _SHARED_PREFIX + '推断',
    _SHARED_PREFIX + '支持',
    _SHARED_PREFIX + '支撑',
    _SHARED_PREFIX + '断定',
    _SHARED_PREFIX + '确认',
    _SHARED_PREFIX + '视为',
    _SHARED_PREFIX + '解释',
    _SHARED_PREFIX + '认定',
    _SHARED_PREFIX + '证明',
    _SHARED_PREFIX + '说明',
    '不足以建立',
    '不足以据此',
    '为了保持严谨',
    '为了保持表述严谨',
    '为了避免无条件化',
    '为了避免误解',
    '为避免把局部结论扩大',
    '仅作为参考',
    '仅作参考',
    '仅供一般信息',
    '仅可视为',
    '仅就.*?而言',
    '仅能支持',
    '仅能确认',
    '仅能表明',
    '仅能说明',
    '仅适用于',
    '仅限于',
    '仍不能',
    '仍有其他解释空间',
    '仍有解释空间',
    '会把确定性说得过高',
    '会把范围放得过大',
    '低概率仍可能发生',
    '保留.*?可能',
    '保留修正空间',
    '保留其他解释空间',
    '先不下结论',
    '先保留判断',
    '具体情况具体分析',
    '具体问题具体分析',
    '只是初步判断',
    '只是可能解释',
    '只是合理推断',
    '只是局部支持',
    '只是工作假设',
    '只是工作结论',
    '只是暂定判断',
    '只是有力线索',
    '只是观察结果',
    '只是阶段性判断',
    '只能作为',
    '只能做有限判断',
    '只能形成暂定结论',
    '只能支持',
    '只能确认',
    '只能算是',
    '只能给出.*?判断',
    '只能表明',
    '只能视为',
    '只能说明',
    '只能适用于',
    '只适用于',
    '只限于',
    '因果关系尚未',
    '因果方向仍',
    '因果链尚未',
    '如果严格一点说',
    '如果更谨慎一点',
    '存在其他解释',
    '存在替代解释',
    '存在竞争性解释',
    '尚不能',
    '尚不足以',
    '尚待定性',
    '尚未定论',
    '常见做法也不必然是最优做法',
    '常见情况不能代替具体判断',
    '并非一定',
    '并非唯一解释',
    '并非总是',
    '并非所有情况下',
    '并非无条件',
    '并非普遍',
    '并非最终结论',
    '并非最终证明',
    '并非每次',
    '并非绝对',
    '应留有余地',
    '应限制在',
    '应限定在',
    '建议咨询专业人士',
    '归因仍不唯一',
    '归因仍需谨慎',
    '归因尚早',
    '当前最多只能说到',
    '当前结论属于阶段性判断',
    '无法排除',
    '无法证明',
    '无法说明',
    '无法锁定唯一',
    '旧(?:计划|方案|标准|规则).*?(?:仅作为|只作为|仅作|只作|仅供|不再作为|不作为)',
    '暂不下结论',
    '暂不作进一步判断',
    '暂不外推',
    '暂不定性',
    '暂不定论',
    '暂不封闭其他解释',
    '暂不延伸',
    '暂不能',
    '暂保留判断',
    '更严谨的表述是',
    '更保守的表述是',
    '更保守的说法是',
    '更倾向于',
    '更稳妥的表述是',
    '更稳妥的说法是',
    '更适合视为',
    '替代解释仍待排除',
    '最多能支持',
    '最多说明关联',
    '最多说明存在关联',
    '有效范围限于',
    '未来仍有变数',
    '没有绝对答案',
    '没有统一答案',
    '现有结论不是永久承诺',
    '现阶段更像是',
    '理论上仍可能',
    '目前只能表述为',
    '目前只能说',
    '经验规律不是定律',
    '经验规律不是硬性定律',
    '结论尚早',
    '结论应保持开放',
    '结论最好停在',
    '至多支持',
    '至多能支持',
    '至多说明',
    '行业惯例也不必然适合当前情况',
    '规范允许也不必然代表最佳实践',
    '超过已验证范围',
    '辩证看待',
    '还不能',
    '还有其他解释空间',
    '这(?:并)?不等于',
    '这(?:并)?不等同于',
    '这(?:并)?不表示',
    '这只是基于现有材料的推断',
    '这只是对当前问题的有限回答',
    '这只是当前最优解释',
    '这只是当前条件下的验证',
    '这并非对全部情况的保证',
    '这样说容易让人误解为',
    '这样说容易造成过度概括',
    '这种结论超过了已验证范围',
    '这里有一个重要限定',
    '适用范围限于',
    '避免绝对化',
    '避免过度',
    '需要保留一个条件',
    '需要保留余地',
    '需要加一个限定',
    '需要把措辞限制在',
    '需要把结论收窄',
    '需要留有余地',
    '需要避免把.*?说成',
    '高概率仍可能不发生',
    '默认行为也不必然适用于特殊场景',
)))


def check(text):
    if not text.strip():
        raise ValueError("候选正文为空。")
    if "\x00" in text or "\x01" in text:
        raise ValueError("候选文件含保留控制字符，请另存为 UTF-8 文本。")
    issues = {}
    for unit in prose_units(text):
        unit = re.sub(r"[\u200b\u200c\u200d\ufeff]", "", unit)
        unit = re.sub(r"(?<=[\u3400-\u9fff])\s+(?=[\u3400-\u9fff])", "", unit)
        for found in re.finditer(r"[^。！？!?\x00]+[。！？!?]?", unit):
            sentence = found.group().strip()
            matches = {match.group() for pattern in PATTERNS
                       for match in pattern.finditer(sentence) if match.group()}
            if matches:
                issues.setdefault(sentence, set()).update(matches)
    return {"status": "blocked" if issues else "clear",
            "hits": [{"text": text, "matches": sorted(matches)}
                     for text, matches in issues.items()]}


if __name__ == "__main__":
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(encoding="utf-8", errors="backslashreplace")
    try:
        if len(sys.argv) != 2 or sys.argv[1] == "-":
            raise ValueError("请指定完整候选文件：python reply_check.py reply.md")
        result = check(Path(sys.argv[1]).read_text(encoding="utf-8-sig"))
        code = 1 if result["status"] == "blocked" else 0
    except Exception as error:
        result, code = {"status": "error", "error": str(error)}, 2
    print(json.dumps(result, ensure_ascii=False, separators=(",", ":")))
    sys.exit(code)
```
