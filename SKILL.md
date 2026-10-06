---
name: speak-properly
description: "用于 chatbot 中文回复的表达取舍与修订。起草时使用内嵌方法，发送前实际运行内嵌 Python 检查完整候选。"
---

```python
#!/usr/bin/env python3
# 好好说话 · Chatbot Version
#
# 适用：chatbot 生成或改写中文回复，包括解释、建议、讨论和交付说明。
# 输入：当前用户请求、与请求有关的已有对话和待发送的完整回复。
# 本文件包含全部表达方法及检查代码，不需要其他技能文件。静默使用。
# 事实依据仍须来自任务资料；本技能检查表达，代码不能判断事实是否可靠。
#
# 写作方法
# 从用户明确提出的问题起笔，把回答、理由与必要条件接起来。对话中没有给出的
# 动机只能作为待确认的猜测，不能把自己的推测宣布为用户的真正需求。
# 篇幅由理解所需的信息决定：保留关键关系、事实和例子，删掉没有作用的铺垫。
# 用户要详细推导时展开推导；简单问题直接回答，不能用简短为理由删掉关键条件。
#
# 解释时指出具体对象、发生的变化和条件。用术语能更准确地交流时保留术语；
# 如果术语只是把一个容易理解的过程藏起来，就把过程写出来。
# 案例：用户问冰箱为什么停一会儿又启动。
# 坏稿：这是设备通过闭环调节机制平衡热环境与运行负荷的表现。
# 好稿：箱内温度降到设定下限，压缩机就停；升到上限，它再启动。停机后，
#       外界热量仍会通过箱壁和门封进入，开门也会带进暖空气，所以温度会回升。
# 理由：温度变化、启停条件和热量来源解释了现象；抽象名词本身没有解释这些关系。
#
# 案例：用户只问备份放在哪里。
# 坏稿：这不是迁移，而是备份；它不代表原文件被删除。
# 好稿：备份在你指定的目录 F:/ 里。
# 理由：这段对比没有回答额外问题；若用户确实追问原文件是否还在，再据实回答。
# 示例中的位置、日志和检测条件仅在各自假设中成立，实际回复必须使用当前任务资料。
#
# 建议应写清动作如何解决当前问题。不能把“优化机制、重塑流程、加强闭环”
# 当作解决办法；需要指出哪个环节发生了什么、准备改变什么，以及为什么有用。
# 案例：用户问怎样避免重复提交表单。
# 坏稿：需要从提交链路入手，完善防重机制。
# 好稿：发送请求后先禁用提交按钮，收到结果再恢复，避免等待期间反复点击。
#       如果请求可能被重试，还要由服务端识别同一次提交，避免重复写入。
# 理由：动作与重复发生的条件一一对应；第二项只在请求重试也需要防重时展开。
#
# 汇报交付时说明结果、与用户目标有关的变化及影响使用的未完成事项。
# 路径、错误和必须逐字保留的引用保持准确。链接应有足以辨认内容的名称。
# 不主动堆砌“我检查过、没有碰其他文件”等自证；用户质疑某项操作时据实回应那项。
# 案例：用户让整理会议记录。
# 坏稿：我已完成整理，检查了格式，没有删除合同，也没有修改其他文件。
# 好稿：会议记录已按议题整理，待办列在文末。负责人未明确的事项已标出。
# 理由：结果和待办状态影响使用；无关操作清单占用注意力。实际没有待办时不要照抄。
#
# 总结提示词只允许出现在首个可见正文行。这里的正文行排除 Markdown 标题、引用块和
# 代码块。若“简而言之、总结来说、核心是、一句话、概括、准确地、结论”出现在后文，
# 必须拦截：要么把对应结论提到首个可见正文行，要么删除提示词或整段重复内容。
# 若把结论前移，必须继续检查后文是在补充新信息，还是只换说法复述；仅复述就删除。
# 即使提示词已经位于首个可见正文行，也要判断去掉提示词、只保留结论本身是否更简洁
# 干练；如果含义和语气不受损，就删掉这些形式化用词。
#
# 运行约定
# 将本代码块保存为 reply_check.py。
# 成稿后必须实际执行：python3 reply_check.py reply.md，reply.md 为完整候选（UTF-8）。
# blocked 必须改写，error 须修复后重跑；review 提示结合当前请求判断，可保留必要表达。
# 按上述写作方法复核全文，clear 也不例外；正文改动后重测，通过后发送受检原文。
# 改写须保留事实及必要原文，处理表达问题本身，禁止只改格式规避门禁。

import argparse
import json
import re
import sys
from html.parser import HTMLParser
from pathlib import Path


class VisibleText(HTMLParser):
    BLOCKS = {"p", "div", "li", "blockquote", "pre", "tr", "h1", "h2", "h3", "h4", "h5", "h6"}
    HEADINGS = {"h1", "h2", "h3", "h4", "h5", "h6"}

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.visible = []
        self.prose = []
        self.body = []
        self.protected = 0
        self.heading = 0

    def boundary(self):
        self.visible.append("\n")
        self.prose.append("\n")
        self.body.append("\n")

    def handle_starttag(self, tag, attrs):
        if tag in self.BLOCKS or tag == "br":
            self.boundary()
        if tag in self.HEADINGS:
            self.heading += 1
        if tag in {"code", "pre", "blockquote"}:
            self.protected += 1
            self.prose.append("\n")
            self.body.append("\n")
        if tag == "img":
            self.handle_data(dict(attrs).get("alt", ""))

    def handle_endtag(self, tag):
        if tag in {"code", "pre", "blockquote"}:
            self.protected = max(0, self.protected - 1)
            self.prose.append("\n")
            self.body.append("\n")
        if tag in self.HEADINGS:
            self.heading = max(0, self.heading - 1)
        if tag in self.BLOCKS:
            self.boundary()
        elif tag in {"td", "th"}:
            self.boundary()

    def handle_data(self, data):
        self.visible.append(data)
        if not self.protected:
            self.prose.append(data)
            if not self.heading:
                self.body.append(data)


PAIR = re.compile(r"不[^不而。！？!?；;\r\n]{0,30}而")
BANNED = ("你说得对", "过头了", "质疑地对", "还不能")
LEAD_MARKERS = ("简而言之", "总结来说", "核心是", "一句话", "概括", "准确地", "结论")
RULES = (
    ("intent", r"你(?:真正|实际)(?:想要|需要|关心|的问题)|你的(?:真正|实际)需求",
     "是否把推测的动机当成用户已表达的需求？依据不足就回到原问题。"),
    ("contrast", r"不代表|不等于|不能据此|仅限于|不是|并非|而是",
     "对比或限制是否回答实际问题、影响结论？保留必要条件，删除无用纠偏。"),
    ("abstract", r"(?:优化|完善|重塑|加强|建立).{0,10}(?:机制|流程|闭环)|(?:底层逻辑|本质上)",
     "是否只给抽象名称？补出当前对象的变化、条件或具体动作。"),
    ("self_proof", r"(?:我已|已经|逐项|全部).{0,8}(?:检查|验证|核对)|没有(?:修改|删除|触碰)",
     "用户是否在问这项操作？汇报保留影响使用的结果和未完成事项。"),
)


def markdown_structure(markdown, mistune):
    ast = mistune.create_markdown(renderer="ast", plugins=["table"])(markdown)
    present = {"heading": False, "unordered_list": False, "ordered_list": False, "bold": False}

    def visit(nodes):
        for node in nodes:
            node_type = node.get("type")
            attrs = node.get("attrs", {})
            if node_type == "heading" and node.get("style") == "atx":
                present["heading"] = True
            elif node_type == "list":
                if attrs.get("ordered"):
                    present["ordered_list"] = True
                elif node.get("bullet") == "-":
                    present["unordered_list"] = True
            elif node_type == "strong":
                present["bold"] = True
            children = node.get("children")
            if children:
                visit(children)

    visit(ast)
    return present


FORMAT_LABELS = {
    "heading": "Markdown 标题（#）",
    "list": "列表层级（- 或 1.）",
    "bold": "关键加粗（**...**）",
}
FORMAT_THRESHOLD = 500


def lead_marker_check(body):
    lines = [line.strip() for line in body.splitlines() if line.strip()]
    first_line = lines[0] if lines else ""
    first_line_hits = [marker for marker in LEAD_MARKERS if marker in first_line]
    off_first_line = []

    for line_number, line in enumerate(lines[1:], start=2):
        for marker in LEAD_MARKERS:
            if marker in line:
                off_first_line.append({"keyword": marker, "line": line_number, "text": line})

    return {
        "keywords": list(LEAD_MARKERS),
        "first_body_line": first_line,
        "first_line_hits": first_line_hits,
        "off_first_line": off_first_line,
    }


def check(markdown):
    import mistune

    render = mistune.create_markdown(escape=True, plugins=["table"])
    parser = VisibleText()
    parser.feed(render(markdown))
    visible = "".join(parser.visible)
    pairs = PAIR.findall(visible)
    banned = [phrase for phrase in BANNED if phrase in visible]
    prose = "".join(parser.prose)
    body = "".join(parser.body)
    lead_markers = lead_marker_check(body)

    review = []
    for rule, pattern, question in RULES:
        hits = []
        for sentence in re.split(r"[。！？!?；;\r\n]", prose):
            if re.search(pattern, sentence):
                hits.append(sentence.strip())
        if hits:
            review.append({"rule": rule, "passages": list(dict.fromkeys(hits)), "question": question})

    if lead_markers["first_line_hits"]:
        review.append({
            "rule": "lead_marker_style",
            "passages": [lead_markers["first_body_line"]],
            "question": (
                "首行的形式化提示词是否必要？如果去掉关键词、只保留结论会更简洁干练且不损失含义，就删掉关键词。"
                "若这是从后文前移的结论，再检查后文是在补充新信息还是复述；仅复述则删除后文。"
            ),
        })

    letter_count = sum(char.isalpha() for char in visible)
    required = letter_count >= FORMAT_THRESHOLD
    structure = markdown_structure(markdown, mistune)
    present = {
        "heading": structure["heading"],
        "list": structure["unordered_list"] or structure["ordered_list"],
        "bold": structure["bold"],
        "unordered_list": structure["unordered_list"],
        "ordered_list": structure["ordered_list"],
    }
    missing = [
        FORMAT_LABELS[name]
        for name in ("heading", "list", "bold")
        if required and not present[name]
    ]
    formatting_blocked = bool(missing)
    lead_marker_blocked = bool(lead_markers["off_first_line"])
    wording_blocked = len(pairs) >= 2 or bool(banned)
    blocked = lead_marker_blocked or formatting_blocked or wording_blocked

    next_steps = []
    if lead_marker_blocked:
        next_steps.append(
            "将命中的总结提示词移到首个可见正文行或删除；若前移，检查后文是否只复述该结论，仅复述就删除。"
            "前移后再判断去掉提示词是否更简洁，能删则删。"
        )
    if formatting_blocked:
        next_steps.append("补齐缺失的 Markdown 分层表达后重测：" + "、".join(missing) + "。")
    if wording_blocked:
        next_steps.append("改写命中的禁用措辞或重复对比后重测。")
    if not blocked:
        next_steps.append("结合写作方法判断全文与提示项；修改则重测，未修改则交付受检原文。")

    return {
        "status": "blocked" if blocked else "review" if review else "clear",
        "pairs": {"count": len(pairs), "matches": pairs},
        "banned": banned,
        "lead_markers": lead_markers,
        "formatting": {
            "letter_count": letter_count,
            "threshold": FORMAT_THRESHOLD,
            "required": required,
            "present": present,
            "missing": missing,
        },
        "review": review,
        "next": " ".join(next_steps),
    }, 1 if blocked else 0


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input", nargs="?", default="-")
    args = parser.parse_args()
    markdown = sys.stdin.read() if args.input == "-" else Path(args.input).read_text(encoding="utf-8-sig")
    if not markdown.strip():
        raise ValueError("候选正文为空")
    result, code = check(markdown)
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as error:
        print(json.dumps({"status": "error", "error": str(error), "next": "解决执行错误后重跑；无法执行则报告检查受阻。"}, ensure_ascii=False))
        sys.exit(2)
```
