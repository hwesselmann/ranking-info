package de.hdawg.rankinginfo.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackages = "de.hdawg.rankinginfo.web")
public class GlobalWebExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public String handleIllegalArgument(IllegalArgumentException e, RedirectAttributes redirect) {
    redirect.addFlashAttribute("danger", e.getMessage());
    return "redirect:/";
  }

  @ExceptionHandler(Exception.class)
  public String handleException(Exception e, RedirectAttributes redirect) {
    redirect.addFlashAttribute("danger", "Ein Fehler ist aufgetreten");
    return "redirect:/";
  }
}
