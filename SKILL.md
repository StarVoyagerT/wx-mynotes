---
name: answer-me
description: "凡是 chatbot 需要回复用户都用该技能，根据当前请求组织通俗自然的中文回复，并在发送前实际运行内嵌 Python 实测器检查完整候选；仅读取规则不算使用。本技能触发时禁止声明。"
---

```python
#!/usr/bin/env python3
# 执行与发送条件
#
# 本技能以当前回复的实际代码执行为完成条件。以下注释是 chatbot 必须遵守的工作指令；复制本代码块为 reply_check.py 后，运行其中的完整实现。
#
# 1. 先生成本轮准备发送的完整候选回复，再通过沙箱的代码或终端工具实际运行本实测器；每次调用必须真正启动 Python 执行下方完整实测器。
# 2. 输入必须是准备发送的完整原文，包括标题、列表、引用、代码和链接；禁止用摘要、样例、截断文本或另一版回复代测。
# 3. 必须取得本次工具调用返回的 JSON 和实际退出码。阅读规则、查看源码、脑内估算、口头声称已检查、手写检查结果，以及复用往轮结果，均不构成执行证据。
# 4. 根据本次结果处理全部命中：OVER_NEGATION_PAIRS 必须重写；OVER_BUDGET 和 OVER_DENSITY 按本轮高细节豁免条件处理；SELF_REVIEW_REQUIRED 必须按返回的 self_review 逐项自审；同时完成 actions 中的处理要求。
# 5. 准备发送的原文一旦改动，必须重跑新候选；原文未变且本次所有适用门禁与自审均已完成时，直接发送已检查的原文。
# 6. 空输出、执行错误或缺少工具返回时，技能尚未完成；先定位并解决。沙箱缺少执行能力或所需依赖且当前授权无法解决时，只如实报告具体阻塞，禁止将其记为通过或已完成使用。
#
# 执行记录留在工具结果中，用户回复保持静默使用。以下发送条件约束 chatbot 的行为；实测器只有实际运行后才能检查输入，单靠这份文件无法替宿主调用工具或在宿主层阻止发送。
#
#
# # 同频交流
#
# ## 定义
#
# 同频交流是一套供 chatbot 使用、面向中文用户的回应方法：只纳入足以回答当前请求的内容，并按照用户的熟悉程度和对话语气表达已经核实的任务事实与专业判断。
#
# ## 边界
#
# - 当前轮用户明确、字面要求‘详细讲讲’‘深入分析’‘展开说明’‘全面分析’‘完整解释’等高细节输出时，**本轮**跳过页数与密度门禁，仍运行实测器并完成否定措辞自审。不得根据语气、主题或模型推断触发；**不得跨轮继承**。否定表达不触发赦免。
# - 本技能追求的是**同频**，不奖励过度精简。只要回复在预算内，就优先保留有用解释、判断依据、必要例子和自然语气；不得为了更短而删除仍有增量的信息，或把回答压成电报式结论。
# - 只约束宿主智能体**面向用户**的语言，不改变智能体之间的内部交流。
# - 保持已经观察到的**事实和结论不变**：可运行或阻塞、PASS 或 FAIL、已完成或缺失的工作进度，都不得改变。
# - 可以补充解释、重新组织信息并加入有**证据支持的判断**；不得为了简化回复而编造证据或改写事实。
# - **静默使用**，禁止声明正在使用本技能。
#
# ## 通俗表达
#
# - 根据对话估计用户对该领域的熟悉程度。熟悉的术语无需解释；无法确定时优先使用直白措辞，不要扩写成基础教程。
# - 命令、路径、错误、日志或测试结果一旦被选入回复，就保留其原始内容，只改写周围的说明文字。
# - 在选定的回复规模内积极给出有用的专业意见。事实与意见混淆会影响用户判断时，说明意见的依据。
# - 只有当抽象词说明了它归纳的具体事实以及保留的关键区别时，才允许它承担解释。否则，改为陈述它所掩盖的事实、关系或原因。
# - 需要判断时，开头 200 字内先说清关键因果。篇幅受限时，聊天仍须自足地交代核心结论、关键因果、当前状态和用户需要的下一步；压缩会损失必要信息时，将有独立交付价值的细节另存文档，其余冗余内容删除。
#
# ## 样本
#
# 以下样本展示回复与请求不相称的几类情况：解释规模超过当前问题的需要，用术语遮住用户需要理解的过程或变化，或擅自扩大用户命题再作防御。它们不要求压缩必要信息；高细节输出的适用条件见“边界”。
#
# ### 定义
#
# **输入**：Python 中的类是什么？
#
# **Bad**：类提供了把数据和功能绑定在一起的方法。创建新类时创建了新的对象类型，从而能够创建该类型的新实例。实例具有能维持自身状态的属性，还具有能修改自身状态的方法（由其所属的类来定义）。
#
# **Good**：类是一种对象模板。它规定按这一模板创建出来的对象应具备哪些属性，以及可调用哪些方法。
#
#
#
# **输入**：机会成本是什么？
#
# **Bad**：机会成本是经济学中的一个基本概念，指选择某一方案时，所放弃的最佳替代方案能够带来的价值。它可以表现为金钱、时间、体验或其他收益。例如，周末选择加班而放弃看演出，观看演出所能带来的体验就是这一选择的机会成本。
#
# **Good**：假设周末只能选加班或看演出，选了加班，错过演出带来的体验就是机会成本。这个概念关心的是：同一份时间或资源，拿去做另一件值得的事，本来能得到什么。
#
# ### 机制
#
# **输入**：冰箱为什么会自己停机，过一会儿又启动？
#
# **Bad**：冰箱压缩机的间歇性运行，源于设备对内部热环境实施的闭环调节机制。温度传感单元持续采集箱内状态，控制模块将监测结果与预设阈值进行比较，并据此切换制冷执行部件的工作状态。该过程通过周期性启停压缩机，在维持目标温度区间的同时，兼顾能源消耗与设备运行负荷。
#
# **Good**：冰箱是看温度来决定什么时候制冷的：降到设定的下限，压缩机就停；升到上限，它再启动。
# 压缩机停下来以后，外面的热量会通过箱壁和门封传进去，导致里面慢慢变暖；开门拿东西时，暖空气也会进去。等温度升到上限，压缩机才开始制冷，这样冰箱里的温度就能保持在设定范围内。
#
# > 坏回答把“什么东西在什么条件下做什么”藏进了专业名词里，读者得自己翻译。好回答直接写出温度变化、压缩机启停以及它们之间的关系，让读者能够顺着过程理解。迁移到其他解释任务时，检查读者是否还得自己把术语还原成实际过程。
#
#
#
# **输入**：自行车停踩后为什么还能往前走？
#
# **Bad**：自行车停止踩踏后仍然能够向前运动，主要是由于惯性的作用。根据牛顿第一定律，物体在合外力为零时会保持静止或匀速直线运动状态，而停止踩踏只是停止提供驱动力，自行车仍然具有向前的速度。在实际骑行中，滚动阻力、机械摩擦和空气阻力会使车速逐渐降低，直至停止。
#
# **Good**：牛顿第一定律说，物体受到的合力为零时，会保持静止或匀速直线运动；所以你停下踩踏，车原有的速度还在，会继续往前滑。实际骑车时，摩擦和空气阻力一直在拖慢它，最后它才会停下来。
#
# ### 边界
#
# **输入**：保温杯能一直保温吗？
#
# **Bad**：保温杯所提供的温度保持能力具有明确的时间与环境边界。真空夹层能够降低热传导和对流造成的能量交换，但杯盖、密封结构及杯体材料仍会形成热量传递路径。在内外温差持续存在的情况下，杯中液体最终仍将趋向环境温度。因此，所谓“保温”应理解为延缓温度变化，而非维持恒定温度。
#
# **Good**：不能，按热力学第二定律，热量会自发地从温度高的地方传向温度低的地方，所以热水会向较冷的周围环境散热。保温杯靠真空夹层等结构减慢这个过程，但仍有热量通过杯盖、杯口等地方散出去，放久了水就会凉。
#
#
#
# **输入**：平均分高，能说明每个人的分数都高吗？
#
# **Bad**：平均分反映的是一组数据的整体水平，不能直接说明每个个体的具体情况，因为较高的分数可能拉高平均值。例如，60、60、100分的平均分约为73.3分，但其中两人的分数均低于这一水平。还可以结合中位数观察数据的分布：将这三个分数排序后，位于中间的数值为60分。
#
# **Good**：光看平均分，看不出每个人考得怎样。比如60、60、100分，平均约73分，但这组成绩的中位数只有60分。
#
# ### 请求
#
# **情境**：用户希望让明日方舟在 SakuraCat 开着时走直连。代理已准备好新设置，正在请求用户批准应用。
#
# **Bad**：是否批准通过本机接口热加载这份候选配置？
#
# **Good**：我准备让正在运行的 SakuraCat 读取新设置，让明日方舟走直连。现在应用吗？
#
# > 用户要决定是否同意，坏回答却让用户先理解实现方式。解释机制时，让用户理解过程；请求批准时，让用户理解要做什么、会产生什么变化。技术细节的取舍取决于当前问题：如果用户正在排查接口调用失败，“本机接口”和具体命令就可能是必要信息。
#
# ### 报告
#
# **情境**：代理完成本地 Skill 安装后，检查项目文档，发现两处原有文字被检查器标记。用户需要知道具体问题及其处理情况。
#
# **Bad**：项目全文文字检查仍有第 16、22 行两处旧命中，本次保留。
#
# **Good**：项目根目录的 AGENTS.md 有两处原有文字被检查器标记：
#
# - 第 16 行说明修饰语的判断标准时，多加了一层否定和转折，末句可以直接写成“判断标准是这段修饰是否改变了读者对事实、条件、因果或程度的理解”。
# - 第 22 行在说明哪些纠偏句式可以使用，检查器也标记了其中引用的句式示例。
#
# 这两处超出本次安装任务的修改范围，暂时保留。
#
# > 坏回答省掉了文件、具体问题和处理理由，用户只能追问或自己翻找记录。将“旧命中”换成“原有文字被标记”，仍然缺少这些信息。写提醒时，检查用户是否能从对话和回复中知道这件事指什么、影响什么、如何处理；凡是理解所必需、却只存在于工具日志或代理执行经历中的信息，应补进回复。具体补哪些信息取决于这项提醒要帮助用户作什么判断。
#
# ### 防御性
#
# **输入**：小明昨天只吃了午餐的一碗面，其他时间都在沉迷打游戏，后来他说自己晚上很饿，但妈妈已经睡觉了，所以没人给他做饭。请问一定能推出小明昨天怎么了？
#
# **Bad**：以上证据能够证明小明昨天饿肚子了，但不能代表小明每天都在饿肚子。
#
# **Good**：小明昨天饿肚子了。
#
# > 只问“小明昨天怎么了”，多余尾巴却另立“每天都如此”的主张再否定。自审时应删除这项用户没有提出、也无需澄清的范围扩张。
#
#
#
# **输入**：数据库索引是干什么的？用个比喻讲讲。
#
# **Bad**：如果只是帮助建立直观印象，用书的目录来类比数据库索引是比较合适的，理解到它能辅助定位数据就可以，二者在具体实现上仍有差异。
#
# **Good**：数据库索引像书的目录：先查到要找的内容在哪里，再直接翻过去，能省去从头到尾查找的工夫。
#
# > 这里要改掉的是“先限定理解层次，再评价这种理解够不够”整套说法；把实际作用、过程或例子重新放到句子的中心。
#
# ### “不……而”数量硬门禁
#
# - 对渲染后可见的整条回复按字面计数，包括引用和代码；“不”与“而”之间最多允许 30 个字符，标点和空格也计入，不跨越 `。！？!?；;` 或换行。
# - 按从左到右、非重叠方式匹配，每个“不”只配对后续第一个满足上述条件的“而”；中途再出现“不”，从新的“不”开始匹配。匹配时保留渲染文本的原始字符间距与句段分隔。
# - 累计达到 2 对时，实测器输出 `OVER_NEGATION_PAIRS`、数量及命中片段，强制阻止发送；必须在保持事实和必要信息的前提下重写，并重新实测至少于 2 对。
# - 此门禁独立于否定措辞自审，高细节豁免和自审保留理由均不能豁免；少于 2 对后，仍须完成其余适用检查。
#
#
# ## 检查
#
# 首次使用时，将本文件的整个 Python 代码块原样保存为 reply_check.py，注释与实现一起保留；更新本文件时同步覆盖该脚本。每轮从保存路径运行磁盘上的固定脚本，传入准备发送的完整候选回复。
#
# 在脚本所在的沙箱工作目录，使用现有 Python 执行：
#
#     python3 reply_check.py reply.md
#
# reply.md 为 UTF-8 编码的完整候选回复。也可通过标准输入传入完整候选：
#
#     python3 reply_check.py < reply.md
#
# 按返回的 actions 和 self_review 完成处理，正文改动后重跑。结果为 PASS 时发送；要求自审时，完成自审并确认所有适用门禁通过后发送；高细节豁免按“边界”执行，“不……而”配对门禁始终生效。
#
# ## 运行与估算边界
#
# - 脚本和候选文件保存在沙箱允许的目录。示例中的 python3 使用当前沙箱已有或宿主指定的 Python；依赖为 mistune，页数估算模块已内嵌，其余依赖来自 Python 标准库。
# - 复用已确认的环境；输入、路径、依赖或权限报错时查明原因，按宿主授权规则处理后重跑。运行报错或空输出时，检查尚未完成。
# - 输入位置参数省略或为 - 时读取标准输入；第二个可选位置参数保存 HTML 预览，例如 python3 reply_check.py reply.md preview.html。预览用于人工查看，常规检查由 Python 直接完成。
# - 退出码 0 为通过，1 为篇幅、密度或配对数量超限，2 为执行错误，3 为仅需否定措辞自审；组合问题逐项处理，以 verdict、actions 和 self_review 为准。
# - page_measurement=estimated 表示页数来自固定模板的估算：正文宽 666 像素，每页可用高度 856 像素，基准字号 16、行高 1.72；分别计算正文、标题、列表、引用、代码块和表格的高度。
# - ASCII 字宽使用固定模板在 16 像素字号下的测量值，其他字符及表格列宽采用近似值。接近分页临界点时可能与 HTML 预览相差一页，字体环境变化也会影响精度。
# - 图片按 HTML 中明确的高度计算，缺少高度时使用 180 像素的占位估计；自定义 HTML 样式也可能影响估算精度。
# - 比较耗时时，区分脚本输出的 elapsed_ms 和工具总耗时。临时输入和可选预览遵循宿主的文件清理规则；仅在维护或排错时读取实现。

import time

STARTED = time.perf_counter()

import argparse
import json
import re
import sys
from pathlib import Path

from mistune import html
import math
import re
import unicodedata
from dataclasses import dataclass, field
from html.parser import HTMLParser


CONTENT_WIDTH = 750 - 42 * 2
PAGE_HEIGHT = 1000 - 48 * 2 - 30 - 18
FONT_SIZE = 16
LINE_HEIGHT = 1.72
ASCII_WIDTHS_AT_16PX = (
    4.391, 4.547, 6.281, 9.453, 8.625, 13.094, 12.813, 3.688, 4.828, 4.828,
    6.672, 10.953, 3.469, 6.406, 3.469, 6.234, 8.625, 8.625, 8.625, 8.625,
    8.625, 8.625, 8.625, 8.625, 8.625, 8.625, 3.469, 3.469, 10.953, 10.953,
    10.953, 7.172, 15.281, 10.328, 9.172, 9.906, 11.219, 8.094, 7.813, 10.984,
    11.359, 4.266, 5.719, 9.281, 7.531, 14.375, 11.969, 12.063, 8.969, 12.063,
    9.578, 8.5, 8.391, 11, 9.938, 14.953, 9.438, 8.844, 9.125, 4.828,
    6.063, 4.828, 10.953, 6.641, 4.297, 8.141, 9.406, 7.391, 9.422, 8.375,
    5.016, 9.422, 9.063, 3.875, 3.875, 7.953, 3.875, 13.781, 9.063, 9.375,
    9.406, 9.422, 5.563, 6.797, 5.422, 9.063, 7.672, 11.563, 7.344, 7.75,
    7.234, 4.828, 3.828, 4.828, 10.953,
)
BLOCKS = {"p", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li",
          "blockquote", "pre", "table", "thead", "tbody", "tr", "div", "hr"}
VOID = {"br", "hr", "img", "input", "meta", "link", "wbr"}


@dataclass
class Node:
    tag: str
    attrs: dict = field(default_factory=dict)
    children: list = field(default_factory=list)


class Document(HTMLParser):
    def __init__(self, source):
        super().__init__(convert_charrefs=True)
        self.root = Node("root")
        self.stack = [self.root]
        self.feed(source)

    def handle_starttag(self, tag, attrs):
        node = Node(tag, dict(attrs))
        self.stack[-1].children.append(node)
        if tag not in VOID:
            self.stack.append(node)

    def handle_startendtag(self, tag, attrs):
        self.handle_starttag(tag, attrs)
        if tag not in VOID:
            self.handle_endtag(tag)

    def handle_endtag(self, tag):
        for i in range(len(self.stack) - 1, 0, -1):
            if self.stack[i].tag == tag:
                del self.stack[i:]
                break

    def handle_data(self, data):
        self.stack[-1].children.append(data)


def visible_text(node):
    if isinstance(node, str):
        return node
    if node.tag in {"script", "style"}:
        return ""
    if node.tag == "br":
        return "\n"
    text = "".join(visible_text(child) for child in node.children)
    if node.tag in {"td", "th"}:
        return text + "\t"
    return text + ("\n" if node.tag in BLOCKS else "")


def char_width(char, size, mono=False):
    if unicodedata.combining(char) or unicodedata.category(char) in {"Cf", "Cc"}:
        return 0
    if unicodedata.east_asian_width(char) in {"W", "F"}:
        return size
    if mono:
        return size * 0.6
    if 32 <= ord(char) <= 126:
        return ASCII_WIDTHS_AT_16PX[ord(char) - 32] * size / 16
    return size * (0.63 if char.isupper() else 0.52)


def inline_tokens(node, size=FONT_SIZE, mono=False, bold=False):
    if isinstance(node, str):
        for part in re.findall(r"[A-Za-z0-9_]+|\s+|.", node):
            text = " " if part.isspace() else part
            yield text, sum(char_width(c, size, mono) for c in text) * (1.04 if bold else 1)
        return
    if node.tag in {"script", "style"}:
        return
    if node.tag == "br":
        yield "\n", 0
    for child in node.children:
        yield from inline_tokens(child, size, mono or node.tag == "code",
                                 bold or node.tag in {"strong", "b", "th"})


def inline_height(node, width, size=FONT_SIZE):
    lines, used, previous_space = 1, 0.0, False
    for text, length in inline_tokens(node, size):
        if text == "\n":
            lines, used, previous_space = lines + 1, 0, False
        elif text == " ":
            if used and not previous_space:
                used += length
            previous_space = True
        else:
            previous_space = False
            if used and used + length > width:
                lines, used = lines + 1, 0
            while length > width:
                lines, length = lines + 1, length - width
            used += length
    def images(parent):
        if isinstance(parent, str):
            return 0
        if parent.tag == "img":
            value = parent.attrs.get("height", "") or ""
            return float(value) if re.fullmatch(r"\d+(?:\.\d+)?", value) else 180
        return sum(images(child) for child in parent.children)

    return lines * size * LINE_HEIGHT + images(node)


def block_box(node, width):
    tag = node.tag
    if tag in {"script", "style"}:
        return 0, 0, 0
    if tag == "pre":
        lines = max(1, len(visible_text(node).rstrip("\n").split("\n")))
        return lines * FONT_SIZE * LINE_HEIGHT + 30, 16, 16
    if tag in {"h1", "h2", "h3", "h4", "h5", "h6"}:
        scale = {"h1": 2, "h2": 1.5, "h3": 1.17, "h4": 1, "h5": 0.83, "h6": 0.67}[tag]
        return inline_height(node, width, FONT_SIZE * scale), 26, 12
    if tag == "p":
        return inline_height(node, width), 0, 16
    if tag in {"ul", "ol"}:
        return flow(node.children, max(1, width - 40), include_edges=True), 16, 16
    if tag == "blockquote":
        return flow(node.children, max(1, width - 80)), 16, 16
    if tag == "hr":
        return 2, 8, 8
    if tag == "table":
        rows = []

        def collect(parent):
            for child in parent.children:
                if isinstance(child, Node):
                    if child.tag == "tr":
                        rows.append([c for c in child.children if isinstance(c, Node)
                                     and c.tag in {"td", "th"}])
                    else:
                        collect(child)

        collect(node)
        columns = max((len(row) for row in rows), default=1)
        preferred = [1.0] * columns
        for row in rows:
            for i, cell in enumerate(row):
                preferred[i] = max(preferred[i], sum(n for _, n in inline_tokens(cell)))
        available = max(columns, width - 2 * (columns + 1) - 2 * columns)
        widths = preferred[:]
        if sum(widths) > available:
            minimum = [min(p, FONT_SIZE * 2, available / columns) for p in preferred]
            remaining = available - sum(minimum)
            weights = [p - m for p, m in zip(preferred, minimum)]
            widths = [m + remaining * w / sum(weights) for m, w in zip(minimum, weights)]
        height = sum(max((inline_height(cell, widths[i]) for i, cell in enumerate(row)),
                         default=0) + 2 for row in rows) + 2 * (len(rows) + 1)
        return height, 0, 0
    return flow(node.children, width, include_edges=True), 0, 0


def flow(children, width, include_edges=False):
    boxes, inline = [], []

    def flush():
        if inline and visible_text(Node("span", children=inline)).strip():
            boxes.append((inline_height(Node("span", children=inline), width), 0, 0))
        inline.clear()

    for child in children:
        if isinstance(child, Node) and child.tag in BLOCKS:
            flush()
            boxes.append(block_box(child, width))
        else:
            inline.append(child)
    flush()
    height, previous = 0.0, 0
    for i, (body, top, bottom) in enumerate(boxes):
        height += body + (max(previous, top) if i else top if include_edges else 0)
        previous = bottom
    return height + (previous if include_edges else 0)


def estimate(fragment):
    document = Document(fragment)
    height = flow(document.root.children, CONTENT_WIDTH)
    return {"pages": max(1, math.ceil(height / PAGE_HEIGHT)),
            "height_px": round(height, 1), "page_height_px": PAGE_HEIGHT,
            "text": visible_text(document.root)}


NEGATION_PATTERN = re.compile(r"不|并非|未必|没有|无法|无需|无须")
NEGATION_PAIR_PATTERN = re.compile(r"不[^不而。！？!?；;\r\n]{0,30}而")
REVIEW_QUESTION = "用户的请求需要你澄清或者进行防御了吗？"
MAX_PAGES = 2
MAX_PARAGRAPH_SENTENCES = 2
MAX_SENTENCE_CHARS = 150
REVIEW_INSTRUCTIONS = [
    "在内部逐项指出当前用户请求或上下文中的具体依据，决定删除或保留；不要向用户反问或索取确认。",
    "用户已限定对象、时间或情境时，删除只重复这些限定、未处理实际误解的多余说明；不要只换词保留同一项无关防御。",
    "回答用户实际问题、报告已核实的失败、保留必要原文或纠正实际误解所需的否定表达可以保留；改写须保留事实、结论和必要条件。",
    "自审后正文有改动就重新检查；正文未改时，先确认配对数量门禁通过，再确认篇幅和密度通过或本轮已获高细节豁免；这些条件满足后直接发送，无需重复检测。",
]


def find_negation_reviews(text):
    reviews = []
    for sentence in re.findall(r"[^。！？!?\n]+[。！？!?]?", text):
        markers = list(dict.fromkeys(NEGATION_PATTERN.findall(sentence)))
        if markers:
            reviews.append({"text": sentence.strip(), "matches": markers})
    return reviews


TPL = r"""<!doctype html><meta charset=utf-8><style>
*{box-sizing:border-box}body{margin:0;background:#eee;font-family:ui-sans-serif,-apple-system,BlinkMacSystemFont,"Segoe UI",Helvetica,Arial,sans-serif;color:#242424}
.v{width:750px;height:1000px;margin:auto;background:#fff;display:flex;flex-direction:column;overflow:hidden}
.b{height:48px;flex:none;padding:15px 18px;background:#fafafa;font-size:13px;color:#666}.e{flex:1;min-height:0;padding:30px 42px 18px;overflow:hidden}.q{height:100%;overflow:hidden}
.c{font-size:16px;line-height:1.72;overflow-wrap:anywhere}.c p{margin:0 0 16px}.c h1,.c h2,.c h3{margin:26px 0 12px}.c code{font-family:monospace;background:#f3f3f3}.c pre{padding:15px 17px;background:#f6f6f6;overflow:auto}
.nav{height:48px;flex:none;display:flex;align-items:center;justify-content:space-between;padding:0 18px;background:#fafafa}button{padding:7px 12px}
</style><div class=v><div class=b>reply.md</div><main class=e><div id=q class=q><article id=c class=c>{{CONTENT}}</article></div></main>
<div class=nav><button id=p>上一页</button><span id=i></span><button id=n>下一页</button></div></div>
<script>
let k=0,H=q.clientHeight,N=Math.ceil(c.scrollHeight/H);window.R={pages:N};
function s(){c.style.transform=`translateY(${-k*H}px)`;i.textContent=`${k+1} / ${N}`;p.disabled=!k;n.disabled=k==N-1}
p.onclick=()=>{k--;s()};n.onclick=()=>{k++;s()};s()
</script>"""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input", nargs="?", default="-")
    parser.add_argument("preview", nargs="?")
    args = parser.parse_args()
    md = sys.stdin.read() if args.input == "-" else Path(args.input).read_text(encoding="utf-8")
    if not md.strip():
        raise ValueError("候选回复为空")

    prose = re.sub(r"```.*?```", "", md, flags=re.S)
    prose = re.sub(r"(?m)^\s*(?:[-*]|\d+[.)])\s+", "\n\n", prose)
    pars = [p.strip() for p in re.split(r"\n\s*\n", prose) if p.strip()]
    badp = []
    bads = []
    for i, p in enumerate(pars, 1):
        count = len(re.findall(r"[。！？!?]", p))
        if count > MAX_PARAGRAPH_SENTENCES:
            badp.append({"paragraph": i, "text": p, "count": count,
                         "limit": MAX_PARAGRAPH_SENTENCES})
        for j, sentence in enumerate(re.split(r"[。！？!?]+", p), 1):
            length = len(re.sub(r"[\W_]", "", sentence))
            if length > MAX_SENTENCE_CHARS:
                bads.append({"paragraph": i, "sentence": j, "text": sentence.strip(),
                             "length": length, "limit": MAX_SENTENCE_CHARS})

    fragment = html(md)
    rendered = estimate(fragment)
    pages = rendered["pages"]
    negation_reviews = find_negation_reviews(rendered["text"])
    negation_pairs = NEGATION_PAIR_PATTERN.findall(rendered["text"])

    layout_issues = (["OVER_BUDGET"] if pages > MAX_PAGES else []) + (["OVER_DENSITY"] if badp or bads else [])
    pair_issues = ["OVER_NEGATION_PAIRS"] if len(negation_pairs) >= 2 else []
    hard_issues = layout_issues + pair_issues
    issues = hard_issues + (["SELF_REVIEW_REQUIRED"] if negation_reviews else [])
    if args.preview:
        Path(args.preview).write_text(TPL.replace("{{CONTENT}}", fragment), encoding="utf-8")
    result = {"verdict": "+".join(issues) or "PASS", "pages": pages, "paragraphs": badp, "sentences": bads,
              "negation_pair_count": len(negation_pairs), "negation_pairs": negation_pairs,
              "page_measurement": "estimated", "height_px": rendered["height_px"],
              "page_height_px": rendered["page_height_px"],
              "elapsed_ms": round((time.perf_counter() - STARTED) * 1000)}
    actions = []
    if pair_issues:
        actions.append("不……而配对达到 2 对：按 negation_pairs 重写后重测至少于 2 对；高细节豁免和自审理由均不能放行。")
    if pages > MAX_PAGES:
        actions.append(f"回复估算为 {pages} 页，上限 {MAX_PAGES} 页；未获本轮高细节豁免时，缩减后重测。")
    if badp or bads:
        actions.append(
            f"按 paragraphs 和 sentences 中的原文修改：每段最多 {MAX_PARAGRAPH_SENTENCES} 个句末标点，"
            f"每句去掉空格和其他标点后最多 {MAX_SENTENCE_CHARS} 字；未获本轮高细节豁免时，修改后重测。"
        )
    if negation_reviews:
        result["self_review"] = {"question": REVIEW_QUESTION, "items": negation_reviews,
                                 "instructions": REVIEW_INSTRUCTIONS}
        actions.append("结合当前对话，按 self_review 的要求逐项完成否定措辞自审。")
    elif layout_issues and not pair_issues:
        actions.append("若本轮已获高细节豁免，可直接发送当前候选；豁免条件见 SKILL.md 的边界。")
    elif not hard_issues:
        actions.append("发送当前候选回复。")
    result["actions"] = actions
    print(json.dumps(result, ensure_ascii=False))
    return 1 if hard_issues else 3 if negation_reviews else 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as error:
        print(json.dumps({"verdict": "ERROR", "error": str(error),
                          "actions": ["修复报错后重新检查；排障见本脚本开头的“运行与估算边界”注释。"]},
                         ensure_ascii=False))
        sys.exit(2)
```
