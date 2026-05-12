# -*- coding: utf-8 -*-
"""
抽奖软件 - 随机抽取名单中的幸运儿
"""

import random
import tkinter as tk
from tkinter import messagebox

# 抽奖名单
NAMES = [
    '郑文哲', '赵江', '于珊', '谢术平', '徐珺烨', '陈宏樑', '唐克勇', '杨帆',
    '童燕', '沈清', '陆益波', '夏燕', '顾晓佳', '陈珏', '聂颖祺', '骆涉宇',
    '周世强', '吴嵩', '丁兆峰', '潘安迪', '支宏愿', '王丽璟', '王敏捷', '朱云',
    '沈懿瑶', '韩君懿', '徐颖文', '李玉霞', '徐辉', '陶如洁', '张浩笛', '郭佳红',
    '蔡诚杰', '方宇轩', '吴慧麟', '邱明'
]

class LotteryApp:
    def __init__(self, root):
        self.root = root
        self.root.title("抽奖软件")
        self.root.geometry("400x500")
        self.root.resizable(False, False)
        
        # 标题
        title_label = tk.Label(root, text="🎉 幸运抽奖 🎉", font=("微软雅黑", 24, "bold"))
        title_label.pack(pady=20)
        
        # 抽奖结果展示
        self.result_label = tk.Label(root, text="?", font=("微软雅黑", 48, "bold"), fg="#FF6B6B")
        self.result_label.pack(pady=20)
        
        # 抽取人数选择
        self.count_label = tk.Label(root, text="抽取人数:", font=("微软雅黑", 12))
        self.count_label.pack()
        
        self.count_var = tk.IntVar(value=1)
        self.count_spinbox = tk.Spinbox(root, from_=1, to=10, textvariable=self.count_var, font=("微软雅黑", 12), width=5)
        self.count_spinbox.pack(pady=5)
        
        # 抽奖按钮
        self.draw_button = tk.Button(root, text="开始抽奖", command=self.draw, 
                                     font=("微软雅黑", 14, "bold"), bg="#4ECDC4", fg="white",
                                     width=15, height=2)
        self.draw_button.pack(pady=20)
        
        # 重置按钮
        self.reset_button = tk.Button(root, text="重置", command=self.reset,
                                      font=("微软雅黑", 10), bg="#95A5A6", fg="white",
                                      width=10, height=1)
        self.reset_button.pack(pady=10)
        
        # 已抽取记录
        self.history_label = tk.Label(root, text="已抽取: 0 人", font=("微软雅黑", 10), fg="#7F8C8D")
        self.history_label.pack(pady=5)
        
        self.drawn_names = set()
        
    def draw(self):
        count = self.count_var.get()
        remaining = [n for n in NAMES if n not in self.drawn_names]
        
        if len(remaining) < count:
            messagebox.showwarning("警告", "剩余可抽取人数不足！")
            return
        
        winners = random.sample(remaining, count)
        
        for winner in winners:
            self.drawn_names.add(winner)
        
        # 显示结果
        if count == 1:
            self.result_label.config(text=winners[0])
        else:
            self.result_label.config(text="、" .join(winners))
        
        # 更新历史记录
        self.history_label.config(text=f"已抽取: {len(self.drawn_names)} 人")
        
        # 检查是否全部抽完
        if len(self.drawn_names) >= len(NAMES):
            messagebox.showinfo("恭喜", "所有人均已抽取完毕！")
            self.draw_button.config(state="disabled")
    
    def reset(self):
        self.drawn_names.clear()
        self.result_label.config(text="?")
        self.history_label.config(text="已抽取: 0 人")
        self.draw_button.config(state="normal")
        self.count_var.set(1)

if __name__ == "__main__":
    root = tk.Tk()
    app = LotteryApp(root)
    root.mainloop()
