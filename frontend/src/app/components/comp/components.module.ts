import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { BaseComponent } from "./base/base.component";
import { ContainerComponent } from "./containers/container.component";
import { ImageComponent } from "./image/image.component";
import { NoteComponent } from "./containers/note/note.component";
import { TextComponent } from "./text/text.component";
import { FormsModule } from "@angular/forms";
import { BoardComponent } from "./containers/board/board.component";
import { ContextMenuComponent } from "./context-menu/context-menu.component";
import { TaskComponent } from "./containers/task/task.component";
import { CalendarComponent } from "./calendar/calendar.component";
import { PromptComponent } from "../prompt/prompt.component";
import { VideoComponent } from "./video/video.component";

@NgModule({
  declarations: [
    BaseComponent, //
    ContainerComponent,
    ImageComponent,
    VideoComponent,
    CalendarComponent,
    NoteComponent,
    TextComponent,
    BoardComponent,
    TaskComponent,
    ContextMenuComponent,
  ],
  imports: [
    CommonModule, //
    FormsModule,
    PromptComponent,
  ],
  exports: [
    // needed within containers
    ContainerComponent,
    BoardComponent,
    ImageComponent,
    VideoComponent,
    CalendarComponent,
    NoteComponent,
    TextComponent,
    TaskComponent,
    ContextMenuComponent,
  ],
})
export class ComponentsModule {}
