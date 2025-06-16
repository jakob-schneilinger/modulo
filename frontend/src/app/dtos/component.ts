export type ComponentType = "board" | "task" | "note" | "text" | "image" | "video" | "sketch" | "calendar";
export interface ComponentNameTypeMap {
  board: Board;
  task: Task;
  note: Note;
  text: Text;
  image: Image;
  video: Video;
  sketch: Sketch;
  calendar: Calendar;
}
export interface Component<T extends ComponentType = any> {
  id?: number;
  type: T;
  ownerId?: number;
  parentId?: number;
  width: number;
  height: number;
  column: number;
  row: number;
}

export interface Container<T extends ComponentType = any> extends Component<T> {
  //name: string;
  children: Component[];
}

export interface Board extends Container<"board"> {
  name: string;
  type: "board";
  depth?: number;
}

export interface Image extends Container<"image"> {
  type: "image";
}

export interface Calendar extends Component<"calendar"> {
  type: "calendar";
  entries: CalendarEntry[];
}

export interface Video extends Component<"video"> {
  type: "video";
}

export interface Sketch extends Component<"sketch"> {
  type: "sketch";
}

export interface Task extends Container<"task"> {
  type: "task";
  name: string;
  repeating: boolean;
  deadLineInDays?: number;
  startDate: Date;
  endDate?: Date;
  completed: boolean;
}

/** DTO representing a note component */
export interface Note extends Container<"note"> {
  type: "note";
  name?: string;
  content?: string;
  labels?: Label[];
}

/** DTO representing a lable used for tagging components */
export interface Label {
  name: string;
  color?: string;
}

/** DTO used for creating an image component */
export interface ImageCreate {
  parentId: number;
  width: number;
  height: number;
  column: number;
  row: number;
}

/** DTO used for creating a calendar component */
export interface CalendarCreate {
  parentId: number;
  width: number;
  height: number;
  column: number;
  row: number;
}

export interface CalendarEntry {
  startDate: Date;
  endDate: Date;
  title: string;
  description: string;
}

export interface Text extends Component<"text"> {
  type: "text";
  content: string;
  // name: string;
  // fontSize: number;
}

export interface GroupComponent<T extends ComponentType = any> {
  id?: number;
  type: T;
  ownerId?: number;
  parentId?: number;
  width: number;
  height: number;
  column: number;
  row: number;
}

export function isText(component: Component): component is Text {
  return component.type === "text";
}
export function isTask(container: Component): container is Task {
  return container.type === "task";
}

export function isImage(component: Component): component is Image {
  return component.type === "image";
}

export function isType<T extends ComponentType>(component: Component, type: T): component is ComponentNameTypeMap[T] {
  return component.type === type;
}

export function isContainer<T extends ComponentType>(component: Component<T>): component is Container<T> {
  return (
    isType(component, "board") || //
    isType(component, "task") ||
    isType(component, "note") ||
    (component as any).children
  );
}
