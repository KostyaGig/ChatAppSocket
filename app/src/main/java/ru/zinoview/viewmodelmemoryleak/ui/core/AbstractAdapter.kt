package ru.zinoview.viewmodelmemoryleak.ui.core

import androidx.recyclerview.widget.ListAdapter

// todo
abstract class AbstractAdapter<T : DiffSame<T>>(
    callback: AbstractDiffUtil<T>
) : ListAdapter<T, AbstractViewHolder<T>>(callback)