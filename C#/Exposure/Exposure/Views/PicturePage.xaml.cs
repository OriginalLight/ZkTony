using Exposure.Contracts.Services;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Exposure.Views;

public sealed partial class PicturePage : Page
{
    public PicturePage()
    {
        ViewModel = App.GetService<PictureViewModel>();
        InitializeComponent();
    }

    public PictureViewModel ViewModel
    {
        get;
    }

    private async void OnLoaded(object sender, RoutedEventArgs e)
    {
        var text = await ViewModel.GetSelectedFolder();
        AutoSuggestBox.Text = text;
    }

    private async void TextChanged(AutoSuggestBox sender, AutoSuggestBoxTextChangedEventArgs args)
    {
        if (args.Reason != AutoSuggestionBoxTextChangeReason.UserInput)
        {
            return;
        }

        var suggestions = ViewModel.Folders.Where(p => p.StartsWith(sender.Text, StringComparison.OrdinalIgnoreCase))
            .ToList();

        if (suggestions.Count > 0)
        {
            sender.ItemsSource = suggestions;
        }
        else
        {
            sender.ItemsSource = new[] { "没有匹配项" };
        }

        await ViewModel.OnFolderChanged(sender.Text);
    }

    private async void QuerySubmitted(AutoSuggestBox sender, AutoSuggestBoxQuerySubmittedEventArgs args) =>
        await ViewModel.OnFolderChanged(args.QueryText);

    private async void SuggestionChosen(AutoSuggestBox sender, AutoSuggestBoxSuggestionChosenEventArgs args) =>
        await ViewModel.OnFolderChanged(sender.Text);

    private void OnItemClick(object sender, ItemClickEventArgs e)
    {
        if (e.ClickedItem == null)
        {
            return;
        }

        var navigationService = App.GetService<INavigationService>();
        navigationService.NavigateTo(typeof(PictureDetailViewModel).FullName!, e.ClickedItem);
    }

    private async void OnSelectedDatesChanged(CalendarView sender, CalendarViewSelectedDatesChangedEventArgs args)
    {
        if (args.AddedDates.Count <= 0)
        {
            return;
        }

        // 获取选择的日期
        var selectedDate = args.AddedDates[0].Date;

        var text = selectedDate.ToString("yyyy-MM-dd");
        AutoSuggestBox.Text = text;
        await ViewModel.OnFolderChanged(text);
    }
}