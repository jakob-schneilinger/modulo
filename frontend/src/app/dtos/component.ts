export interface Component {
  id?: number;
  type: "board" | "task" | "note" | "text" | "image" | "video" | "sketch" | "calender";
  ownerId?: number;
  parentId?: number;
  width: number;
  height: number;
  column: number;
  row: number;
  name?: string;
  children?: Component[];
  tags?: string[];
}

export interface Container extends Component {
  name: string;
  children: Component[];
}

export interface Board extends Container {
  type: "board";
}

export interface Task extends Container {
  type: "task";
  // TODO: implement something with time/deadlines
}

export interface Note extends Container {
  type: "note";
  tags: string[];
}


export interface Text extends Component {
  type: "text";
  text: string;
  name: string;
  fontSize: number;
  parentId: number;
}

export function isText(component: Component): component is Text {
  return component.type === "text";
}
