export interface Item {
  id?: number;
  type: 'board' | 'task' | 'note' | 'text' | 'image' | 'video' | 'sketch' | 'calender';
  owner_id: number;
  width: number;
}


export interface Board extends Item{
  type: 'board';
  width: 2;
  name: string;
  children: Item[];
}

export interface Task extends Item{

}

export interface Note extends Item{

}
