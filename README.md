# TiatCustomStructures

对CustomStructures的拙劣重写，业余选手，码力勿喷

## 修改内容
1. 移除Mask的相关功能
2. 建筑内可生成Adyeshach NPC和MythicMobs 怪物/刷怪点
3. 联动WorldGuard实现生成建筑区域的管理

## 构建发行版本

发行版本用于正常使用, 不含 TabooLib 本体。

```
./gradlew build
```

## 构建开发版本

开发版本包含 TabooLib 本体, 用于开发者使用, 但不可运行。

```
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 -PDeleteCode 表示移除所有逻辑代码以减少体积。